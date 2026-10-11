package com.example.demo.dao;

import com.example.demo.constant.ProductCategory;
import com.example.demo.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
@EnabledIfEnvironmentVariable(named = "MALL_DB_TEST", matches = "true")
class ProductCategoryIntegrationTest {
    @Autowired
    private ProductDao dao;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void matchesDatabaseForAllCategoriesAndUnfilteredList() {
        assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM product", Integer.class),
                dao.getProducts(null).size());
        for (ProductCategory category : ProductCategory.values()) {
            List<Product> products = dao.getProducts(category);
            assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM product WHERE category = ?",
                    Integer.class, category.name()), products.size());
            assertTrue(products.stream().allMatch(p -> p.getCategory() == category));
        }
    }

}
