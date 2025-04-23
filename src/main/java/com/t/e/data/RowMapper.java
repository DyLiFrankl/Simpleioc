package com.t.e.data;

import java.sql.ResultSet;
import java.sql.SQLException;

// 结果映射接口
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException, IllegalAccessException;
}