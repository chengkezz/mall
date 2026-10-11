package com.example.demo.dao;

import com.example.demo.model.Product;
import com.example.demo.dto.ProductRequest;

public interface ProductDao {
    Product getProductById(Integer productId);

    Integer createProduct(ProductRequest productRequest);
}
