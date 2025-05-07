package com.t.e.web;


import com.t.e.util.ConvertUtil;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;



public class AsyncRequestSource implements ParameterSource {
    private final HttpServletRequest request;
    private final Map<String, String[]> parameterMap;
    Map<String, String> singleValueMap = new HashMap<>();
    Collection<Part> parts;
    private final String contentType;
    private String cachedBody;
    
    public AsyncRequestSource(HttpServletRequest request) throws ServletException, IOException {
        this.request = request;
        this.contentType = request.getContentType();
        this.parameterMap = request.getParameterMap();
        this.parameterMap.forEach((key, values) -> {
            if (values != null && values.length > 0) {
                singleValueMap.put(key, values[0]); // 取第一个值
            }
        });

        if (contentType != null) {
            if (contentType.contains("application/json")) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                this.cachedBody = sb.toString();
            } else if (contentType.contains("multipart/form-data")) {
                this.parts = request.getParts(); // 仅适用于文件上传
            } else {
                throw new ServletException("Unsupported Content-Type: " + contentType);
            }
        }
    }

    @Override
    public String getParameter(String name) {
        if(!parameterMap.containsKey(name)) {
            throw new RuntimeException("parameter not exist in http: " + name);
        }
        return parameterMap.get(name)[0];
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public <T> T getBody(Class<T> bodyType, Parameter parameter) throws IOException {

        // 从Param中获取
        if (isJavaLangClass(bodyType)) {
            String paramName = parameter.getName();
            String value = getParameter(paramName);
            return value != null ? (T) ConvertUtil.convertBasicType(value, bodyType) : null;
        }

        // JSON处理逻辑保持不变
        if (cachedBody != null) {
            Object p =  ConvertUtil.convertValue(cachedBody,bodyType,"", parameter);
            return (T) p;
        }

        // 否则走对象构建逻辑
        return buildFromFormData(bodyType);

    }

    /**
     * 从表单字段反射构造对象
     */
    private <T> T buildFromFormData(Class<T> bodyType) {
        // 如果是基本类型/包装类型，直接通过参数获取
        if (isJavaLangClass(bodyType)) {
            String[] values = parameterMap.get(bodyType.getSimpleName().toLowerCase());
            if (values != null && values.length > 0) {
                return (T) ConvertUtil.convertBasicType(values[0], bodyType);
            }
            return null;
        }

        // 处理自定义POJO
        try {
            T instance = bodyType.getDeclaredConstructor().newInstance();
            for (Field field : bodyType.getDeclaredFields()) {
                field.setAccessible(true);
                String[] values = parameterMap.get(field.getName());
                if (values != null && values.length > 0) {
                    Object convertedValue = ConvertUtil.parseObject(values[0], field.getType());
                    field.set(instance, convertedValue);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("构建对象失败: " + bodyType.getSimpleName(), e);
        }
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

