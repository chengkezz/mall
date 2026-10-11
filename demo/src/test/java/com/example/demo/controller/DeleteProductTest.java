package com.example.demo.controller;

import com.example.demo.dao.ProductDao;
import com.example.demo.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DeleteProductTest {
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
    void repeatedDeletesReturn204WithoutLookingUpProduct() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(delete("/products/123"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }
        verify(dao, times(2)).deleteProduct(123);
        verifyNoMoreInteractions(dao);
    }

    @Test
    void rejectsNonIntegerId() throws Exception {
        mvc.perform(delete("/products/abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(dao);
    }
}
