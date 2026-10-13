package com.example.demo.service;
import com.example.demo.model.Order;
import java.util.List;

import com.example.demo.dto.CreateOrderRequest;

public interface OrderService {
    Integer createOrder(Integer userId, CreateOrderRequest request);

    List<Order> getOrders(Integer userId);
}
