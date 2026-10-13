package com.example.demo.service.impl;

import com.example.demo.dao.*;
import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Component
public class OrderServiceImpl implements OrderService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private OrderDao orderDao;

    @Transactional
    @Override
    public Integer createOrder(Integer userId, CreateOrderRequest request) {
        if (userDao.getUserById(userId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        try {
            // 合併重複商品，並固定扣庫存順序以降低死鎖機會。
            Map<Integer, Integer> quantities = new TreeMap<>();
            for (BuyItem item : request.getBuyItemList()) {
                quantities.merge(item.getProductId(), item.getQuantity(), Math::addExact);
            }
            int totalAmount = 0;
            List<OrderItem> items = new ArrayList<>();
            for (var entry : quantities.entrySet()) {
                Product product = productDao.getProductById(entry.getKey());
                if (product == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
                }
                if (productDao.decreaseStock(entry.getKey(), entry.getValue()) == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Product unavailable or insufficient stock");
                }
                int amount = Math.multiplyExact(product.getPrice(), entry.getValue());
                totalAmount = Math.addExact(totalAmount, amount);
                OrderItem item = new OrderItem();
                item.setProductId(entry.getKey());
                item.setQuantity(entry.getValue());
                item.setAmount(amount);
                items.add(item);
            }
            Integer orderId = orderDao.createOrder(userId, totalAmount);
            orderDao.createOrderItems(orderId, items);
            return orderId;
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity or amount exceeds integer limit", exception);
        }
    }

    @Override
    public List<Order> getOrders(Integer userId) {
        if (userDao.getUserById(userId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<Order> orderList = orderDao.getOrders(userId);
        for (Order order : orderList) {
            List<OrderItem> items = orderDao.getOrderItemsByOrderId(order.getOrderId());
            order.setOrderItemList(items);
        }
        return orderList;
    }
}
