package com.example.demo.controller;

import com.example.demo.constant.ProductCategory;
import com.example.demo.dao.ProductDao;
import com.example.demo.model.Product;
import com.example.demo.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GetProductsTest {
    private ProductDao dao;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        dao = mock(ProductDao.class);
        ProductServiceImpl service = new ProductServiceImpl();
        ReflectionTestUtils.setField(service, "productDao", dao);
        ProductController controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productService", service);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsAllProductsAsJsonArray() throws Exception {
        Product first = new Product();
        first.setProductId(1);
        first.setProductName("Apple");
        first.setCategory(ProductCategory.FOOD);
        Product second = new Product();
        second.setProductId(3);
        second.setProductName("Java book");
        second.setCategory(ProductCategory.E_BOOK);
        when(dao.getProducts()).thenReturn(List.of(first, second));

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].category").value("FOOD"))
                .andExpect(jsonPath("$[1].productName").value("Java book"));
        verify(dao).getProducts();
    }

    @Test
    void returnsEmptyArrayWhenNoProductsExist() throws Exception {
        when(dao.getProducts()).thenReturn(List.of());
        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(dao).getProducts();
    }
}
