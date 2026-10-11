package com.example.demo.dao;

import com.example.demo.constant.ProductCategory;
import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "MALL_DB_TEST", matches = "true")
class ProductCreationIntegrationTest {
    @Autowired
    private ProductService productService;

    @Test
    void insertsAndReadsProductWithGeneratedIdAndTimestamps() {
        ProductRequest request = new ProductRequest();
        request.setProductName("新增商品整合測試");
        request.setCategory(ProductCategory.E_BOOK);
        request.setImageUrl("https://example.com/test.jpg");
        request.setPrice(0);
        request.setStock(10);

        Integer id = productService.createProduct(request);
        Product product = productService.getProductById(id);

        assertNotNull(id);
        assertTrue(id > 0);
        assertNotNull(product);
        assertEquals(request.getProductName(), product.getProductName());
        assertEquals(request.getCategory(), product.getCategory());
        assertEquals(request.getImageUrl(), product.getImageUrl());
        assertEquals(request.getPrice(), product.getPrice());
        assertEquals(request.getStock(), product.getStock());
        assertNull(product.getDescription());
        assertNotNull(product.getCreatedDate());
        assertEquals(product.getCreatedDate(), product.getLastModifiedDate());
    }
}
