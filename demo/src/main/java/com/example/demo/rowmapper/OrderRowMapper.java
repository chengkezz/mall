package com.example.demo.rowmapper;

import com.example.demo.model.Order;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderRowMapper implements RowMapper<Order> {
    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        Order result = new Order();
        result.setOrderId(rs.getInt("order_id"));
        result.setUserId(rs.getInt("user_id"));
        result.setTotalAmount(rs.getInt("total_amount"));
        result.setCreatedDate(rs.getTimestamp("created_date"));
        result.setLastModifiedDate(rs.getTimestamp("last_modified_date"));
        return result;
    }
}
