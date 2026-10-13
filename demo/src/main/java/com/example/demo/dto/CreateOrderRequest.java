package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public class CreateOrderRequest {
    @NotEmpty
    @Valid
    private List<@NotNull BuyItem> buyItemList;

    public List<@NotNull BuyItem> getBuyItemList() { return buyItemList; }
    public void setBuyItemList(List<@NotNull BuyItem> buyItemList) { this.buyItemList = buyItemList; }
}
