package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {
    private ProductService productService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        ProductController controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productService", productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsProductWhenFound() throws Exception {
        Product product = new Product();
        product.setProductId(1);
        product.setProductName("Apple");
        product.setPrice(30);
        when(productService.getProductById(1)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Apple"))
                .andExpect(jsonPath("$.price").value(30));
        verify(productService).getProductById(1);
    }

    @Test
    void returnsNotFoundWhenMissing() throws Exception {
        when(productService.getProductById(999)).thenReturn(null);

        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void rejectsNonIntegerId() throws Exception {
        mockMvc.perform(get("/products/abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }
}
