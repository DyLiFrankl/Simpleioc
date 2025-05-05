package com.t.e.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConvertUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Object convertValue(String value, Class<?> targetType, String defaultValue,
                                      Parameter parameter) {
        if (value == null || value.isEmpty()) {
            value = defaultValue;
            if (value == null) return null; // 无默认值且输入为空时返回null
        }

        try {
            // 处理基本类型
            if (targetType == String.class) return value;
            else if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
            else if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
            else if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);

                // 处理集合类型
            else if (List.class.isAssignableFrom(targetType)) {
                return parseList(value, parameter);
            }

            // 处理Map类型
            else if (Map.class.isAssignableFrom(targetType)) {
                return parseMap(value, parameter);
            }

            // 处理自定义对象
            else if (!targetType.isPrimitive() && !targetType.isArray()) {
                return parseObject(value, targetType);
            }

            throw new UnsupportedOperationException("Unsupported type: " + targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Convert failed for [" + value + "] to type " + targetType, e);
        }
    }


    private static Object convertValue(String[] values, Class<?> targetType,
                                       String defaultValue, Parameter parameter) {
        if (values == null || values.length == 0) {
            return convertValue(defaultValue, targetType, defaultValue, parameter);
        }

        // 处理数组/集合类型
        if (targetType.isArray()) {
            return Arrays.stream(values)
                    .map(v -> convertBasicType(v, targetType.getComponentType()))
                    .toArray(size -> (Object[]) Array.newInstance(targetType.getComponentType(), size));
        }
        else if (List.class.isAssignableFrom(targetType)) {
            Class<?> elementType = getGenericType(parameter, 0);
            return Arrays.stream(values)
                    .map(v -> convertBasicType(v, elementType))
                    .collect(Collectors.toList());
        }

        // 默认取第一个值
        return convertBasicType(values[0], targetType);
    }

//    private static Object convertValues(String[] values, Method method, Parameter parameter) {
//        // 复用之前的转换逻辑
//
//    }
    /**
     * 解析List类型（支持泛型，如 List<String> 或 List<User>）
     */
    private static List<?> parseList(String value, Parameter parameter) {
        try {
            // 获取List的泛型类型（如 List<User> 中的 User）
            Class<?> elementType = getGenericType(parameter, 0); // 0表示取第1个泛型参数

            // JSON数组格式（如 [1,2,3] 或 [{"name":"Alice"}]）
            if (value.startsWith("[")) {
                return objectMapper.readValue(
                        value,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)
                );
            }
            // 逗号分隔格式（如 "1,2,3" 或 "name=Alice,age=25"）
            else {
                return Arrays.stream(value.split(","))
                        .map(item -> convertValue(item.trim(), elementType, "", null))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse List: " + value, e);
        }
    }

    /**
     * 解析Map类型（支持泛型）
     */
    private static Map<?, ?> parseMap(String value, Parameter parameter) {
        try {
            // 获取Map的Value类型（如 Map<String, User> 中的 User）
            Class<?> valueType = getGenericType(parameter, 1); // 1表示取第2个泛型参数

            // 处理JSON格式（如 {"key1":"value1", "key2":{"name":"Alice"}}）
            if (value.startsWith("{")) {
                return objectMapper.readValue(
                        value,
                        objectMapper.getTypeFactory().constructMapType(
                                Map.class,
                                String.class, // Key固定为String
                                valueType     // Value类型由泛型决定
                        )
                );
            }
            // 处理键值对格式（如 key1:value1,key2:value2）
            else {
                return Arrays.stream(value.split(","))
                        .map(pair -> pair.split(":", 2)) // 最多分割成2部分
                        .filter(kv -> kv.length == 2)    // 过滤无效格式
                        .collect(Collectors.toMap(
                                kv -> kv[0].trim(),
                                kv -> convertValue(kv[1].trim(), valueType, "", null)
                        ));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Map: " + value, e);
        }
    }

    /**
     * 获取方法参数的泛型类型
     * @param parameter 方法参数
     * @param index 泛型参数索引（0=List的元素类型，1=Map的Value类型等）
     */
    private static Class<?> getGenericType(Parameter parameter, int index) {
        try {
            Type parameterType = parameter.getParameterizedType();
            if (parameterType instanceof ParameterizedType) {
                Type[] actualTypes = ((ParameterizedType) parameterType).getActualTypeArguments();
                if (actualTypes.length > index) {
                    Type type = actualTypes[index];
                    // 处理普通类（如 List<String> 中的 String）
                    if (type instanceof Class) {
                        return (Class<?>) type;
                    }
                    // 处理嵌套泛型（如 List<Map<String, User>>）
                    else if (type instanceof ParameterizedType) {
                        Type rawType = ((ParameterizedType) type).getRawType();
                        if (rawType instanceof Class) {
                            return (Class<?>) rawType;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常，返回默认类型
        }
        return Object.class; // 默认返回Object类型（兼容无泛型的情况）
    }

    /**
     * 解析自定义对象（支持嵌套字段）
     */
    private static Object parseObject(String value, Class<?> targetType) {
        try {
            // JSON格式（如 {"name":"Alice", "age":25}）
            if (value.startsWith("{")) {
                return objectMapper.readValue(value, targetType);
            }
            // 键值对格式（如 name=Alice&age=25&address.city=Beijing）
            else {
                Object instance = targetType.getDeclaredConstructor().newInstance();
                Arrays.stream(value.split("&"))
                        .map(pair -> pair.split("=", 2))
                        .filter(kv -> kv.length == 2)
                        .forEach(kv -> setNestedField(instance, kv[0], kv[1]));
                return instance;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse " + targetType.getSimpleName() + ": " + value, e);
        }
    }

    /**
     * 递归设置嵌套字段值（支持多级路径，如 "user.address.city"）
     * @param target 目标对象（如 User 实例）
     * @param fieldPath 字段路径（如 "address.city"）
     * @param fieldValue 字段值（如 "Beijing"）
     */
    private static void setNestedField(Object target, String fieldPath, String fieldValue) {
        try {
            String[] paths = fieldPath.split("\\.");
            Object current = target;

            // 遍历除最后一级外的所有路径（如 ["address"]）
            for (int i = 0; i < paths.length - 1; i++) {
                Field field = getDeclaredField(current.getClass(), paths[i]);
                field.setAccessible(true);

                // 如果嵌套对象未初始化，则创建实例
                if (field.get(current) == null) {
                    Object nestedObj = field.getType().getDeclaredConstructor().newInstance();
                    field.set(current, nestedObj);
                }
                current = field.get(current); // 进入下一层级
            }

            // 设置最终字段的值（如 "city"）
            Field finalField = getDeclaredField(current.getClass(), paths[paths.length - 1]);
            finalField.setAccessible(true);
            Object convertedValue = convertBasicType(fieldValue, finalField.getType());
            finalField.set(current, convertedValue);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to set field [" + fieldPath + "] with value [" + fieldValue + "]", e);
        }
    }

    /**
     * 获取字段（包括父类字段）
     */
    private static Field getDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 尝试从父类查找
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getDeclaredField(superClass, fieldName);
            }
            throw e;
        }
    }

    /**
     * 基本类型转换（String -> int/boolean等）
     */
    public static Object convertBasicType(String value, Class<?> targetType) {
        if (value == null) return null;

        try {
            if (targetType == String.class) return value;
            else if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
            else if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
            else if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
            else if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
            else if (Enum.class.isAssignableFrom(targetType)) {
                // 处理枚举类型
                return Enum.valueOf((Class<? extends Enum>) targetType, value);
            }
            throw new UnsupportedOperationException("Unsupported field type: " + targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot convert [" + value + "] to type " + targetType.getSimpleName(), e);
        }
    }

}
