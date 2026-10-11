package com.example.demo.dao;

import com.example.demo.model.Product;

public interface ProductDao {
    Product getProductById(Integer productId);
}
