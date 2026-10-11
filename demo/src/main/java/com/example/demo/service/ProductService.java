package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.constant.ProductCategory;
import java.util.List;
import com.example.demo.dto.ProductRequest;

public interface ProductService {
    Product getProductById(Integer productId);

    Integer createProduct(ProductRequest productRequest);

    void updateProduct(Integer productId, ProductRequest productRequest);

    void deleteProduct(Integer productId);

    List<Product> getProducts(ProductCategory category, String search);
}
