package com.example.demo.dao;

import com.example.demo.dto.UserRegisterRequest;
import com.example.demo.model.OrderItem;
import com.example.demo.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "MALL_DB_TEST", matches = "true")
class OrderQueryIntegrationTest {
    @Autowired OrderService service;
    @Autowired OrderDao orders;
    @Autowired UserDao users;
    @Autowired JdbcTemplate jdbc;

    @Test
    void filtersUserSortsOrdersAndMapsEachOrdersDetails() {
        int userId = createUser();
        int otherUser = createUser();
        assertTrue(service.getOrders(userId).isEmpty());
        int older = orders.createOrder(userId, 20);
        int newer = orders.createOrder(userId, 100);
        int tied = orders.createOrder(userId, 30);
        int excluded = orders.createOrder(otherUser, 999);
        jdbc.update("UPDATE `order` SET created_date='2026-01-01 00:00:00' WHERE order_id=?", older);
        jdbc.update("UPDATE `order` SET created_date='2026-01-02 00:00:00' WHERE order_id IN (?,?)", newer, tied);
        // 使用自行建立的商品，避免依賴既有資料。
        String name = "query-test-" + UUID.randomUUID();
        jdbc.update("INSERT INTO product (product_name,category,image_url,price,stock,created_date,last_modified_date) VALUES (?, 'FOOD', 'https://example.com/test.jpg', 10, 10, NOW(), NOW())", name);
        int productId = jdbc.queryForObject("SELECT product_id FROM product WHERE product_name=?", Integer.class, name);
        orders.createOrderItems(newer, List.of(item(productId, 4, 40), item(productId, 6, 60)));
        orders.createOrderItems(older, List.of(item(productId, 2, 20)));
        var result = service.getOrders(userId);
        assertEquals(List.of(tied, newer, older), result.stream().map(o -> o.getOrderId()).toList());
        assertTrue(result.stream().allMatch(o -> o.getUserId().equals(userId)));
        assertTrue(result.get(0).getOrderItemList().isEmpty());
        var order = result.get(1);
        assertEquals(100, order.getTotalAmount());
        assertNotNull(order.getCreatedDate());
        assertNotNull(order.getLastModifiedDate());
        var details = order.getOrderItemList();
        assertEquals(2, details.size());
        assertTrue(details.get(0).getOrderItemId() < details.get(1).getOrderItemId());
        assertEquals(newer, details.get(0).getOrderId());
        assertEquals(productId, details.get(0).getProductId());
        assertEquals(4, details.get(0).getQuantity());
        assertEquals(40, details.get(0).getAmount());
        assertEquals(20, result.get(2).getOrderItemList().get(0).getAmount());
        assertEquals(excluded, service.getOrders(otherUser).get(0).getOrderId());
    }

    int createUser() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("order-query-" + UUID.randomUUID() + "@example.com");
        request.setPassword("test-only");
        return users.createUser(request);
    }
    OrderItem item(int productId, int quantity, int amount) {
        OrderItem item = new OrderItem();
        item.setProductId(productId); item.setQuantity(quantity); item.setAmount(amount);
        return item;
    }
}
