package com.example.demo.service;

import com.example.demo.model.Product;
import java.util.List;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductQueryParams;

public interface ProductService {
    Product getProductById(Integer productId);

    Integer createProduct(ProductRequest productRequest);

    void updateProduct(Integer productId, ProductRequest productRequest);

    void deleteProduct(Integer productId);

    List<Product> getProducts(ProductQueryParams productQueryParams);
}
