package com.example.demo.dao.impl;

import com.example.demo.dao.ProductDao;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductQueryParams;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.util.Date;
import com.example.demo.model.Product;
import com.example.demo.rowmapper.ProductRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductDaoImpl implements ProductDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Product getProductById(Integer productId) {
        String sql = """
                SELECT product_id, product_name, category, image_url, price, stock,
                       description, created_date, last_modified_date
                FROM product
                WHERE product_id = :productId
                """;

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);

        List<Product> list = namedParameterJdbcTemplate.query(
                sql, map, new ProductRowMapper());

        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Integer createProduct(ProductRequest productRequest) {
        String sql = """
                INSERT INTO product (product_name, category, image_url, price, stock,
                                     description, created_date, last_modified_date)
                VALUES (:productName, :category, :imageUrl, :price, :stock,
                        :description, :createdDate, :lastModifiedDate)
                """;

        Date now = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("productName", productRequest.getProductName());
        map.put("category", productRequest.getCategory().name());
        map.put("imageUrl", productRequest.getImageUrl());
        map.put("price", productRequest.getPrice());
        map.put("stock", productRequest.getStock());
        map.put("description", productRequest.getDescription());
        map.put("createdDate", now);
        map.put("lastModifiedDate", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map),
                keyHolder, new String[]{"product_id"});
        return keyHolder.getKey().intValue();
    }

    @Override
    public int updateProduct(Integer productId, ProductRequest productRequest) {
        String sql = """
                UPDATE product
                SET product_name = :productName,
                    category = :category,
                    image_url = :imageUrl,
                    price = :price,
                    stock = :stock,
                    description = :description,
                    last_modified_date = :lastModifiedDate
                WHERE product_id = :productId
                """;

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("productName", productRequest.getProductName());
        map.put("category", productRequest.getCategory().name());
        map.put("imageUrl", productRequest.getImageUrl());
        map.put("price", productRequest.getPrice());
        map.put("stock", productRequest.getStock());
        map.put("description", productRequest.getDescription());
        map.put("lastModifiedDate", new Date());

        return namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void deleteProduct(Integer productId) {
        String sql = """
                DELETE FROM product
                WHERE product_id = :productId
                """;

        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public List<Product> getProducts(ProductQueryParams productQueryParams) {
        String sql = """
                SELECT product_id, product_name, category, image_url,
                       price, stock, description, created_date, last_modified_date
                FROM product
                WHERE 1 = 1
                """;

        Map<String, Object> map = new HashMap<>();
        if (productQueryParams.getCategory() != null) {
            sql += " AND category = :category";
            map.put("category", productQueryParams.getCategory().name());
        }

        if (productQueryParams.getSearch() != null) {
            sql += " AND product_name LIKE :search";
            map.put("search", "%" + productQueryParams.getSearch() + "%");
        }

        // 排序欄位與方向已由 Service 驗證允許值。
        sql += " ORDER BY " + productQueryParams.getOrderBy()
                + " " + productQueryParams.getSort();
        sql += ", product_id ASC";
        sql += " LIMIT :limit OFFSET :offset";
        map.put("limit", productQueryParams.getLimit());
        map.put("offset", productQueryParams.getOffset());

        List<Product> productList = namedParameterJdbcTemplate.query(
                sql, map, new ProductRowMapper());
        return productList;
    }
}
