package com.example.demo.dao;

import com.example.demo.constant.ProductCategory;
import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "MALL_DB_TEST", matches = "true")
class ProductUpdateIntegrationTest {
    @Autowired
    private ProductService service;
    @Autowired
    private ProductDao dao;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void updatesOnlyTargetPreservesCreationTimeAndAllowsIdenticalRequests() {
        ProductRequest request = new ProductRequest();
        request.setProductName("Before");
        request.setCategory(ProductCategory.FOOD);
        request.setImageUrl("https://example.com/before.jpg");
        request.setPrice(20);
        request.setStock(10);
        request.setDescription("Old description");
        int id = service.createProduct(request);
        int otherId = service.createProduct(request);
        jdbc.update("UPDATE product SET created_date = '2020-01-01 00:00:00', last_modified_date = '2020-01-01 00:00:00' WHERE product_id = ?", id);
        Product before = service.getProductById(id);
        request.setProductName("After");
        request.setCategory(ProductCategory.E_BOOK);
        request.setImageUrl("https://example.com/after.jpg");
        request.setPrice(0);
        request.setStock(0);
        request.setDescription(null);

        service.updateProduct(id, request);
        Product after = service.getProductById(id);
        assertEquals(id, after.getProductId());
        assertEquals("After", after.getProductName());
        assertEquals(ProductCategory.E_BOOK, after.getCategory());
        assertEquals(request.getImageUrl(), after.getImageUrl());
        assertEquals(0, after.getPrice());
        assertEquals(0, after.getStock());
        assertNull(after.getDescription());
        assertEquals(before.getCreatedDate(), after.getCreatedDate());
        assertTrue(after.getLastModifiedDate().after(before.getLastModifiedDate()));
        assertEquals("Before", service.getProductById(otherId).getProductName());
        assertDoesNotThrow(() -> service.updateProduct(id, request));

        jdbc.update("DELETE FROM product WHERE product_id = ?", id);
        assertEquals(0, dao.updateProduct(id, request));
    }
}
