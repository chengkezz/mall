package com.example.demo.dao;

import com.example.demo.constant.ProductCategory;
import com.example.demo.model.Product;
import com.example.demo.dto.ProductQueryParams;
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
                dao.getProducts(queryParams(null, null)).size());
        for (ProductCategory category : ProductCategory.values()) {
            List<Product> products = dao.getProducts(queryParams(category, null));
            assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM product WHERE category = ?",
                    Integer.class, category.name()), products.size());
            assertTrue(products.stream().allMatch(p -> p.getCategory() == category));
        }
    }

    @Test
    void matchesDatabaseForKeywordAndCombinedFilters() {
        for (String search : List.of("Java", "蘋果", "%", "_", "'", "no-match-7b9e12")) {
            var expected = jdbc.queryForList(
                    "SELECT product_id FROM product WHERE product_name LIKE ?", Integer.class,
                    "%" + search + "%");
            var actual = dao.getProducts(queryParams(null, search)).stream()
                    .map(Product::getProductId).sorted().toList();
            assertEquals(expected.stream().sorted().toList(), actual);
            for (ProductCategory category : ProductCategory.values()) {
                var filteredExpected = jdbc.queryForList(
                        "SELECT product_id FROM product WHERE category = ? AND product_name LIKE ?",
                        Integer.class, category.name(), "%" + search + "%");
                var filteredActual = dao.getProducts(queryParams(category, search)).stream()
                        .map(Product::getProductId).sorted().toList();
                assertEquals(filteredExpected.stream().sorted().toList(), filteredActual);
            }
        }
    }

    private ProductQueryParams queryParams(ProductCategory category, String search) {
        ProductQueryParams params = new ProductQueryParams();
        params.setLimit(Integer.MAX_VALUE);
        params.setOffset(0);
        params.setCategory(category);
        params.setSearch(search);
        return params;
    }

    @Test
    void sortsBothFieldsInBothDirectionsWithIdTieBreaker() {
        List<Product> all = dao.getProducts(queryParams(null, null));
        for (String field : List.of("price", "created_date")) {
            for (String direction : List.of("asc", "desc")) {
                ProductQueryParams params = queryParams(null, null);
                params.setOrderBy(field);
                params.setSort(direction);
                java.util.Comparator<Product> comparator = "price".equals(field)
                        ? java.util.Comparator.comparing(Product::getPrice)
                        : java.util.Comparator.comparing(Product::getCreatedDate);
                if ("desc".equals(direction)) {
                    comparator = comparator.reversed();
                }
                comparator = comparator.thenComparing(Product::getProductId);
                var expected = all.stream().sorted(comparator).map(Product::getProductId).toList();
                var actual = dao.getProducts(params).stream().map(Product::getProductId).toList();
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    void paginatesSortedFilteredResultsAndReturnsEmptyPastEnd() {
        ProductQueryParams params = queryParams(ProductCategory.FOOD, "%");
        params.setOrderBy("price");
        params.setSort("asc");
        var expected = jdbc.queryForList(
                "SELECT product_id FROM product WHERE category = ? AND product_name LIKE ? ORDER BY price ASC, product_id ASC",
                Integer.class, "FOOD", "%%%");
        params.setLimit(2);
        for (int offset = 0; offset <= expected.size() + 2; offset += 2) {
            params.setOffset(offset);
            var actual = dao.getProducts(params).stream().map(Product::getProductId).toList();
            assertEquals(expected.stream().skip(offset).limit(2).toList(), actual);
        }
    }

    @Test
    void countUsesFiltersAndIgnoresPagination() {
        for (ProductCategory category : ProductCategory.values()) {
            for (String search : List.of("%", "Java", "蘋果", "no-match-7b9e12")) {
                ProductQueryParams params = queryParams(category, search);
                params.setLimit(1);
                params.setOffset(Integer.MAX_VALUE);
                Integer expected = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM product WHERE category = ? AND product_name LIKE ?",
                        Integer.class, category.name(), "%" + search + "%");
                assertEquals(expected, dao.countProducts(params));
                assertTrue(dao.getProducts(params).isEmpty());
            }
        }
        assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM product", Integer.class),
                dao.countProducts(queryParams(null, null)));
    }
}
