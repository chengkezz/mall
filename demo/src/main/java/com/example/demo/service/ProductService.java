package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.dto.ProductRequest;

public interface ProductService {
    Product getProductById(Integer productId);

    Integer createProduct(ProductRequest productRequest);
}
