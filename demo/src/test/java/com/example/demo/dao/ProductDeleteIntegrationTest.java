package com.example.demo.dao;

import com.example.demo.constant.ProductCategory;
import com.example.demo.controller.ProductController;
import com.example.demo.dto.ProductRequest;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "MALL_DB_TEST", matches = "true")
class ProductDeleteIntegrationTest {
    @Autowired
    private ProductService service;
    @Autowired
    private ProductController controller;

    @Test
    void deletesOnlyTargetAndReturns204WhenAlreadyMissing() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setProductName("Delete integration test");
        request.setCategory(ProductCategory.FOOD);
        request.setImageUrl("https://example.com/test.jpg");
        request.setPrice(20);
        request.setStock(10);
        int id = service.createProduct(request);
        int otherId = service.createProduct(request);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(delete("/products/{productId}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertNull(service.getProductById(id));
        assertNotNull(service.getProductById(otherId));

        mvc.perform(delete("/products/{productId}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertNotNull(service.getProductById(otherId));
    }
}
