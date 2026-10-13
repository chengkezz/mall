package com.example.demo.dto;

import jakarta.validation.constraints.*;

public class BuyItem {
    @NotNull
    @Min(1)
    private Integer productId;

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    @NotNull
    @Min(1)
    private Integer quantity;

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
