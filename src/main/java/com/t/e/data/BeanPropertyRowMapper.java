package com.t.e.data;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 自动映射到 Bean 的 RowMapper
public class BeanPropertyRowMapper<T> implements RowMapper<T> {
    private final Class<T> mappedClass;
    private final Map<String, Field> fieldMap = new ConcurrentHashMap<>();

    public BeanPropertyRowMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        // 缓存字段元数据
        for (Field field : mappedClass.getDeclaredFields()) {
            field.setAccessible(true);
            fieldMap.put(field.getName(), field);
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException, IllegalAccessException {
        T obj = instantiateClass(mappedClass);
        ResultSetMetaData metaData = rs.getMetaData();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnLabel(i);
            String fieldName = snakeToCamel(columnName);
            Field field = fieldMap.get(fieldName);

            if (field != null) {
                Object value = rs.getObject(i);
                if (value instanceof java.sql.Timestamp) {
                    value = ((java.sql.Timestamp) value).toLocalDateTime(); // 转换为 LocalDateTime
                }
                field.set(obj, value);
            }
        }
        return obj;
    }

    // 下划线转驼峰（snake_case → camelCase）
    private String snakeToCamel(String snakeCase) {
        StringBuilder camelCase = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < snakeCase.length(); i++) {
            char c = snakeCase.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    camelCase.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    camelCase.append(c);
                }
            }
        }
        return camelCase.toString();
    }


    private T instantiateClass(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new DataAccessException("Failed to instantiate " + clazz.getName(), e);
        }
    }
}
