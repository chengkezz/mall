package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductQueryParams;
import com.example.demo.dto.Page;

public interface ProductService {
    Product getProductById(Integer productId);

    Integer createProduct(ProductRequest productRequest);

    void updateProduct(Integer productId, ProductRequest productRequest);

    void deleteProduct(Integer productId);

    Page<Product> getProducts(ProductQueryParams productQueryParams);
}
