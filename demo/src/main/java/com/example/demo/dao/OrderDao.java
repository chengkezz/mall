package com.example.demo.dao;
import com.example.demo.model.Order;

import com.example.demo.model.OrderItem;
import java.util.List;

public interface OrderDao {
    Integer createOrder(Integer userId, Integer totalAmount);
    void createOrderItems(Integer orderId, List<OrderItem> orderItemList);

    List<Order> getOrders(Integer userId);
    List<OrderItem> getOrderItemsByOrderId(Integer orderId);
}
