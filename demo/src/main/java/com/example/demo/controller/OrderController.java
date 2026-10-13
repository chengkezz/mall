package com.example.demo.controller;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/users/{userId}/orders")
    public ResponseEntity<Map<String, Integer>> createOrder(
            @PathVariable Integer userId,
            @RequestBody @Valid CreateOrderRequest request) {
        Integer orderId = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId", orderId));
    }
}
