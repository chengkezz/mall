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
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CreateProductTest {
    private ProductDao productDao;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productDao = mock(ProductDao.class);
        ProductServiceImpl service = new ProductServiceImpl();
        ReflectionTestUtils.setField(service, "productDao", productDao);
        ProductController controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productService", service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOOD", "CAR", "E_BOOK"})
    void createsProductAndReturnsGeneratedId(String category) throws Exception {
        Product product = new Product();
        product.setProductId(42);
        product.setCategory(ProductCategory.valueOf(category));
        product.setProductName("Test");
        when(productDao.createProduct(any())).thenReturn(42);
        when(productDao.getProductById(42)).thenReturn(product);

        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(category, "0", "0")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(42))
                .andExpect(jsonPath("$.category").value(category))
                .andExpect(jsonPath("$.productName").value("Test"));
        ArgumentCaptor<ProductRequest> request = ArgumentCaptor.forClass(ProductRequest.class);
        verify(productDao).createProduct(request.capture());
        assertEquals(ProductCategory.valueOf(category), request.getValue().getCategory());
        assertEquals(0, request.getValue().getPrice());
        assertNull(request.getValue().getDescription());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"productName\":\" \",\"category\":\"FOOD\",\"imageUrl\":\"url\",\"price\":1,\"stock\":1}",
            "{\"productName\":\"Test\",\"imageUrl\":\"url\",\"price\":1,\"stock\":1}",
            "{\"productName\":\"Test\",\"category\":\"FOOD\",\"price\":1,\"stock\":1}",
            "{\"productName\":\"Test\",\"category\":\"FOOD\",\"imageUrl\":\"url\",\"stock\":1}",
            "{\"productName\":\"Test\",\"category\":\"FOOD\",\"imageUrl\":\"url\",\"price\":1}",
            "null"
    })
    void rejectsMissingFields(String body) throws Exception {
        assertBadRequest(body);
    }

    @Test
    void rejectsNegativeNumbersAndUnknownCategory() throws Exception {
        assertBadRequest(validBody("FOOD", "-1", "1"));
        assertBadRequest(validBody("FOOD", "1", "-1"));
        assertBadRequest(validBody("UNKNOWN", "1", "1"));
    }

    @Test
    void rejectsOversizedFields() throws Exception {
        String body = validBody("FOOD", "1", "1");
        assertBadRequest(body.replace("Test", "x".repeat(129)));
        assertBadRequest(body.replace("https://example.com/image.jpg", "x".repeat(257)));
        assertBadRequest(body.replace("}", ",\"description\":\"" + "x".repeat(1025) + "\"}"));
    }

    private void assertBadRequest(String body) throws Exception {
        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productDao);
    }

    private String validBody(String category, String price, String stock) {
        return """
                {"productName":"Test","category":"%s","imageUrl":"https://example.com/image.jpg",
                 "price":%s,"stock":%s}
                """.formatted(category, price, stock);
    }
}
