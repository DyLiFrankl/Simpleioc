package com.t.e.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 示例：处理枚举类型
public class EnumTypeHandler<E extends Enum<E>> implements TypeHandler<E> {
    private final Class<E> enumType;

    public EnumTypeHandler(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, E param) throws SQLException {
        ps.setString(i, param.name());
    }

    @Override
    public E getResult(ResultSet rs, String columnName) throws SQLException {
        return Enum.valueOf(enumType, rs.getString(columnName));
    }
}