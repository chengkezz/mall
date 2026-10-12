package com.example.demo.controller;

import com.example.demo.dto.ProductQueryParams;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ProductPaginationTest {
    @Autowired
    private WebApplicationContext context;
    @MockitoBean
    private ProductService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(service.getProducts(any())).thenReturn(List.of());
    }

    @Test
    void defaultsAreSuppliedByControllerOnly() throws Exception {
        ProductQueryParams params = new ProductQueryParams();
        assertNull(params.getLimit());
        assertNull(params.getOffset());
        mvc.perform(get("/products")).andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(service).getProducts(argThat(p -> p.getLimit() == 5 && p.getOffset() == 0));
    }

    @ParameterizedTest
    @CsvSource({"1,0", "100,5", "5,2147483647"})
    void acceptsValidBounds(int limit, int offset) throws Exception {
        mvc.perform(get("/products").param("limit", String.valueOf(limit))
                        .param("offset", String.valueOf(offset)))
                .andExpect(status().isOk());
        verify(service).getProducts(argThat(p -> p.getLimit() == limit && p.getOffset() == offset));
    }

    @ParameterizedTest
    @CsvSource({"0,0", "101,0", "-1,0", "5,-1", "abc,0", "5,abc"})
    void rejectsInvalidPaginationBeforeCallingService(String limit, String offset) throws Exception {
        mvc.perform(get("/products").param("limit", limit).param("offset", offset))
                .andExpect(status().isBadRequest());
        verify(service, never()).getProducts(any());
    }
}
