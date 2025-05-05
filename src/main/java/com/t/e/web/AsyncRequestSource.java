package com.t.e.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.t.e.util.ConvertUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.t.e.util.ConvertUtil.convertBasicType;

public class AsyncRequestSource implements ParameterSource {
    private final HttpServletRequest request;
    private String cachedBody;

    public AsyncRequestSource(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getParameter(String name) {
        // 优先尝试普通表单字段
        String value = request.getParameter(name);
        if (value != null) return value;

        // 如果是multipart请求，从Part中读取
        if (isMultipartFormData()) {
            try {
                System.out.println("=== Debug Start ===");
                System.out.println("Content-Type: " + request.getContentType());

                try {
                    Collection<Part> parts = request.getParts();
                    System.out.println("Parts Count: " + parts.size());

                    for (Part part : parts) {
                        System.out.println("Part Name: " + part.getName());
                        System.out.println("Part Size: " + part.getSize());
                        System.out.println("Part Content: " +
                                new BufferedReader(new InputStreamReader(part.getInputStream()))
                                        .lines().collect(Collectors.joining()));
                    }
                } catch (Exception e) {
                    System.out.println("Error reading parts: " + e.getClass().getName() + " - " + e.getMessage());
                }
                Part part = request.getPart(name);
                if (part != null) {
                    return readPartValue(part);
                }
            } catch (Exception ignored) {
                // 忽略Part读取异常
            }
        }
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        // 处理数组型参数（如复选框）
        String[] values = request.getParameterValues(name);
        if (values != null) return values;

        if (isMultipartFormData()) {
            try {
                Collection<Part> parts = request.getParts();
                List<String> result = new ArrayList<>();
                for (Part part : parts) {
                    if (name.equals(part.getName())) {
                        result.add(readPartValue(part));
                    }
                }
                return result.toArray(new String[0]);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // 读取Part的文本内容
    private String readPartValue(Part part) throws IOException {
        try (InputStream is = part.getInputStream()) {
            return new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining());
        }
    }

    @Override
    public <T> T getBody(Class<T> bodyType, Parameter parameter) {
        try {
            if (isMultipartFormData()) {
                // 如果是简单类型，直接调用getParameter
                if (isJavaLangClass(bodyType)) {
                    String paramName = parameter.getName();
                    String value = getParameter(paramName);
                    return value != null ? (T) convertBasicType(value, bodyType) : null;
                }
                // 否则走对象构建逻辑
                return buildFromFormData(bodyType);
            } else {
                // JSON处理逻辑保持不变
                if (cachedBody == null) {
                    cachedBody = readRequestBody(request);
                }
               Object p =  ConvertUtil.convertValue(cachedBody,bodyType,"", parameter);
               return (T) p;
//                return (T) cachedBody;
            }
        } catch (Exception e) {
            throw new RuntimeException("Body解析失败: " + e.getMessage(), e);
        }
    }
    // 辅助方法：获取类中第一个字段名（简化版）
    private String getFirstFieldName(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        return fields.length > 0 ? fields[0].getName() : "value";
    }
    //-- 新增关键方法 --//
    private boolean isMultipartFormData() {
        String contentType = request.getContentType();
        return contentType != null && contentType.startsWith("multipart/");
    }

    /**
     * 从表单字段反射构造对象
     */
    private <T> T buildFromFormData(Class<T> bodyType) {
        // 如果是基本类型/包装类型，直接通过参数获取
        if (isJavaLangClass(bodyType)) {
            String[] values = request.getParameterValues(bodyType.getSimpleName().toLowerCase());
            if (values != null && values.length > 0) {
                return (T) convertBasicType(values[0], bodyType);
            }
            return null;
        }

        // 处理自定义POJO
        try {
            T instance = bodyType.getDeclaredConstructor().newInstance();
            for (Field field : bodyType.getDeclaredFields()) {
                field.setAccessible(true);
                String[] values = request.getParameterValues(field.getName());
                if (values != null && values.length > 0) {
                    Object convertedValue = convertBasicType(values[0], field.getType());
                    field.set(instance, convertedValue);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("构建对象失败: " + bodyType.getSimpleName(), e);
        }
    }

    //-- 原始方法保留 --//
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 判断是否是Java原生类型/包装类
     */
    private boolean isJavaLangClass(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Boolean.class ||
                clazz == Double.class;
    }
}

