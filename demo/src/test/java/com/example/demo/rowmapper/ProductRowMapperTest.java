package com.example.demo.rowmapper;

import com.example.demo.constant.ProductCategory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductRowMapperTest {

    @ParameterizedTest
    @EnumSource(ProductCategory.class)
    void mapsDatabaseCategoryToEnum(ProductCategory category) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("category")).thenReturn(category.name());

        assertEquals(category, new ProductRowMapper().mapRow(resultSet, 0).getCategory());
    }
}
