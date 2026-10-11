package com.example.demo.controller;

import com.example.demo.constant.ProductCategory;
import com.example.demo.dao.ProductDao;
import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Product;
import com.example.demo.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UpdateProductTest {
    private ProductDao dao;
    private MockMvc mvc;
    private static final String BODY = """
            {"productName":"Updated","category":"CAR","imageUrl":"https://example.com/car.jpg",
             "price":50,"stock":30}
            """;

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
    void returnsUpdatedProduct() throws Exception {
        Product updated = new Product();
        updated.setProductId(1);
        updated.setProductName("Updated");
        updated.setCategory(ProductCategory.CAR);
        when(dao.getProductById(1)).thenReturn(new Product(), updated);
        when(dao.updateProduct(eq(1), any())).thenReturn(1);
        mvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Updated"))
                .andExpect(jsonPath("$.category").value("CAR"));
        verify(dao).updateProduct(eq(1), argThat(r -> r.getDescription() == null
                && r.getPrice() == 50 && r.getStock() == 30));
    }

    @Test
    void returns404WhenMissing() throws Exception {
        mvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
        verify(dao, never()).updateProduct(anyInt(), any());
    }

    @Test
    void returns404WhenDeletedBeforeUpdate() throws Exception {
        when(dao.getProductById(1)).thenReturn(new Product());
        when(dao.updateProduct(eq(1), any())).thenReturn(0);
        mvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns404WhenDeletedBeforeReadBack() throws Exception {
        when(dao.getProductById(1)).thenReturn(new Product(), (Product) null);
        when(dao.updateProduct(eq(1), any())).thenReturn(1);
        mvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{\"category\":\"INVALID\"}"})
    void rejectsInvalidBody(String body) throws Exception {
        when(dao.getProductById(1)).thenReturn(new Product());
        mvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verify(dao, never()).updateProduct(anyInt(), any(ProductRequest.class));
    }

    @Test
    void rejectsNegativePrice() throws Exception {
        when(dao.getProductById(1)).thenReturn(new Product());
        mvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("50", "-1")))
                .andExpect(status().isBadRequest());
        verify(dao, never()).updateProduct(anyInt(), any());
    }
}
