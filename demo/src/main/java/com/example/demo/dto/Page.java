package com.example.demo.dto;

import java.util.List;

public class Page<T> {
    private Integer limit;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    private Integer offset;

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    private Integer total;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    private List<T> productlist;

    public List<T> getProductlist() {
        return productlist;
    }

    public void setProductlist(List<T> productlist) {
        this.productlist = productlist;
    }

}
