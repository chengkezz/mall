package com.example.demo.dao;

import com.example.demo.dto.*;
import com.example.demo.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "MALL_DB_TEST", matches = "true")
class OrderIntegrationTest {
    @Autowired OrderService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager manager;

    @Test
    void persistsTotalsDetailsAndStock() {
        // 外層交易建立測試資料；測試結束全部回滾。
        TransactionTemplate transaction = new TransactionTemplate(manager);
        transaction.executeWithoutResult(status -> {
            status.setRollbackOnly();
            String email = "order-" + UUID.randomUUID() + "@example.com";
            jdbc.update("INSERT INTO `user` (email,password,created_date,last_modified_date) VALUES (?, 'test', NOW(), NOW())", email);
            int userId = jdbc.queryForObject("SELECT user_id FROM `user` WHERE email=?", Integer.class, email);
            int first = product(20, 10);
            int second = product(30, 1);
            Integer orderId = service.createOrder(userId, request(first, 2, first, 1, second, 1));
            assertEquals(90, jdbc.queryForObject("SELECT total_amount FROM `order` WHERE order_id=?", Integer.class, orderId));
            assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM order_item WHERE order_id=?", Integer.class, orderId));
            assertEquals(3, jdbc.queryForObject("SELECT quantity FROM order_item WHERE order_id=? AND product_id=?", Integer.class, orderId, first));
            assertEquals(60, jdbc.queryForObject("SELECT amount FROM order_item WHERE order_id=? AND product_id=?", Integer.class, orderId, first));
            assertEquals(7, stock(first));
            assertEquals(0, stock(second));
        });
    }

    @Test
    void failedOrderActuallyRollsBackEarlierStockUpdate() {
        // 在交易外保留測試商品，讓 Service 自己啟動與回滾交易；finally 清除。
        String email = "rollback-" + UUID.randomUUID() + "@example.com";
        Integer userId = null;
        List<Integer> ids = new ArrayList<>();
        try {
            jdbc.update("INSERT INTO `user` (email,password,created_date,last_modified_date) VALUES (?, 'test', NOW(), NOW())", email);
            userId = jdbc.queryForObject("SELECT user_id FROM `user` WHERE email=?", Integer.class, email);
            ids.add(product(20, 10)); ids.add(product(30, 0));
            int id = userId;
            assertThrows(ResponseStatusException.class,
                    () -> service.createOrder(id, request(ids.get(0), 2, ids.get(1), 1)));
            assertEquals(10, stock(ids.get(0)));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM `order` WHERE user_id=?", Integer.class, userId));
        } finally {
            if (userId != null) {
                jdbc.update("DELETE FROM order_item WHERE order_id IN (SELECT order_id FROM `order` WHERE user_id=?)", userId);
                jdbc.update("DELETE FROM `order` WHERE user_id=?", userId);
            }
            for (Integer id : ids) jdbc.update("DELETE FROM product WHERE product_id=?", id);
            jdbc.update("DELETE FROM `user` WHERE email=?", email);
        }
    }

    int product(int price, int stock) {
        String name = "order-test-" + UUID.randomUUID();
        jdbc.update("INSERT INTO product (product_name,category,image_url,price,stock,created_date,last_modified_date) VALUES (?, 'FOOD', 'https://example.com/test.jpg', ?, ?, NOW(), NOW())", name, price, stock);
        return jdbc.queryForObject("SELECT product_id FROM product WHERE product_name=?", Integer.class, name);
    }
    int stock(int id) { return jdbc.queryForObject("SELECT stock FROM product WHERE product_id=?", Integer.class, id); }
    CreateOrderRequest request(int... pairs) {
        List<BuyItem> items = new ArrayList<>();
        for (int i=0; i<pairs.length; i+=2) {
            BuyItem item = new BuyItem(); item.setProductId(pairs[i]); item.setQuantity(pairs[i+1]); items.add(item);
        }
        CreateOrderRequest request = new CreateOrderRequest(); request.setBuyItemList(items); return request;
    }
}
