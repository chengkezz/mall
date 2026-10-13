package com.example.demo.rowmapper;

import com.example.demo.model.OrderItem;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderItemRowMapper implements RowMapper<OrderItem> {
    @Override
    public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        OrderItem result = new OrderItem();
        result.setOrderItemId(rs.getInt("order_item_id"));
        result.setOrderId(rs.getInt("order_id"));
        result.setProductId(rs.getInt("product_id"));
        result.setQuantity(rs.getInt("quantity"));
        result.setAmount(rs.getInt("amount"));
        return result;
    }
}
