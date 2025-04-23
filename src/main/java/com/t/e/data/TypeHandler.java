package com.t.e.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 可添加 TypeHandler 体系处理特殊类型
public interface TypeHandler<T> {
    void setParameter(PreparedStatement ps, int i, T param) throws SQLException;
    T getResult(ResultSet rs, String columnName) throws SQLException;
}
