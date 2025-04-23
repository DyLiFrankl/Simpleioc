package com.t.e.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

public class SerializeUtils {
    public static void serializeValue(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof CharSequence) {
            serializeString(value.toString(), sb);
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Iterable) {
            serializeIterable((Iterable<?>) value, sb);
        } else if (value.getClass().isArray()) {
            serializeArray(value, sb);
        } else if (value instanceof Map) {
            serializeMap((Map<?, ?>) value, sb);
        } else {
            serializeObject(value, sb);
        }
    }

    // 序列化字符串（处理转义字符）
    private static void serializeString(String str, StringBuilder sb) {
        sb.append('"');
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        sb.append('"');
    }

    // 序列化集合类型
    private static void serializeIterable(Iterable<?> iterable, StringBuilder sb) {
        sb.append('[');
        Iterator<?> it = iterable.iterator();
        while (it.hasNext()) {
            serializeValue(it.next(), sb);
            if (it.hasNext()) sb.append(',');
        }
        sb.append(']');
    }

    // 序列化普通对象
    private static void serializeObject(Object obj, StringBuilder sb) {
        sb.append('{');
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);

                if (!first) sb.append(',');
                serializeString(field.getName(), sb);
                sb.append(':');
                serializeValue(value, sb);

                first = false;
            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }
        sb.append('}');
    }
    // 新增方法：处理数组类型
    private static void serializeArray(Object array, StringBuilder sb) {
        sb.append('[');

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            serializeValue(element, sb);
            if (i < length - 1) sb.append(',');
        }

        sb.append(']');
    }

    // 新增方法：处理 Map 类型
    private static void serializeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');

        Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = it.next();

            // 序列化 Key（强制转为字符串）
            serializeString(entry.getKey().toString(), sb);
            sb.append(':');

            // 序列化 Value
            serializeValue(entry.getValue(), sb);

            if (it.hasNext()) sb.append(',');
        }

        sb.append('}');
    }

}
