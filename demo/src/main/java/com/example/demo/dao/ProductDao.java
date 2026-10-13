package com.example.demo.dao;

import com.example.demo.model.Product;
import java.util.List;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductQueryParams;

public interface ProductDao {
    Product getProductById(Integer productId);

    Integer createProduct(ProductRequest productRequest);

    int decreaseStock(Integer productId, Integer quantity);

    int updateProduct(Integer productId, ProductRequest productRequest);

    void deleteProduct(Integer productId);

    List<Product> getProducts(ProductQueryParams productQueryParams);

    Integer countProducts(ProductQueryParams productQueryParams);
}
