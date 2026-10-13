package com.example.demo.model;

public class OrderItem {
    private Integer orderItemId;
    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }
    private Integer orderId;
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    private Integer productId;
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    private Integer quantity;
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    private Integer amount;
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}
