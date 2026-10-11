package com.example.demo.service.impl;

import com.example.demo.dao.ProductDao;
import com.example.demo.dto.ProductRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    public Product getProductById(Integer productId) {
        return productDao.getProductById(productId);
    }

    @Override
    public Integer createProduct(ProductRequest productRequest) {
        validateProductRequest(productRequest);
        return productDao.createProduct(productRequest);
    }

    @Override
    public void updateProduct(Integer productId, ProductRequest productRequest) {
        validateProductRequest(productRequest);
        int updatedRows = productDao.updateProduct(productId, productRequest);
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }

    private void validateProductRequest(ProductRequest productRequest) {
        validateRequiredText(productRequest.getProductName(), "productName", 128);
        validateRequiredText(productRequest.getImageUrl(), "imageUrl", 256);
        if (productRequest.getCategory() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category is required");
        }
        if (productRequest.getPrice() == null || productRequest.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be non-negative");
        }
        if (productRequest.getStock() == null || productRequest.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stock must be non-negative");
        }
        String description = productRequest.getDescription();
        if (description != null && description.codePointCount(0, description.length()) > 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "description must not exceed 1024 characters");
        }
    }

    private void validateRequiredText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        if (value.codePointCount(0, value.length()) > maxLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    field + " must not exceed " + maxLength + " characters");
        }
    }
}
