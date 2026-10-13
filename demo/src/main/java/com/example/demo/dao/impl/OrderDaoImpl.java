package com.example.demo.dao.impl;

import com.example.demo.dao.OrderDao;
import com.example.demo.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class OrderDaoImpl implements OrderDao {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer createOrder(Integer userId, Integer totalAmount) {
        String sql = """
                INSERT INTO `order` (user_id, total_amount, created_date, last_modified_date)
                VALUES (:userId, :totalAmount, :createdDate, :lastModifiedDate)
                """;
        Date now = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("totalAmount", totalAmount);
        map.put("createdDate", now);
        map.put("lastModifiedDate", now);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map),
                keyHolder, new String[]{"order_id"});
        return keyHolder.getKey().intValue();
    }

    @Override
    public void createOrderItems(Integer orderId, List<OrderItem> orderItemList) {
        String sql = """
                INSERT INTO order_item (order_id, product_id, quantity, amount)
                VALUES (:orderId, :productId, :quantity, :amount)
                """;
        for (OrderItem item : orderItemList) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("productId", item.getProductId());
            map.put("quantity", item.getQuantity());
            map.put("amount", item.getAmount());
            namedParameterJdbcTemplate.update(sql, map);
        }
    }
}
