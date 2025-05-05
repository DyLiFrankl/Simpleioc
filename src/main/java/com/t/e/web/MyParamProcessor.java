package com.t.e.web;

import com.mysql.cj.util.StringUtils;
import com.t.e.util.ConvertUtil;

import javax.servlet.http.HttpServletRequest;

import java.lang.reflect.*;
import java.util.*;


public class MyParamProcessor {
    /**
     * 核心方法：处理方法的参数绑定
     * @param method 目标方法
     * @param args 参数数组（会被修改）
     * @param requestParams 请求参数（模拟 Map<String, String>）
     */
    public static void process(Method method, Object[] args, Map<String, String> requestParams,ParameterSource parameterSource) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            RequestParam annotationParam = parameters[i].getAnnotation(RequestParam.class);
            RequestBody annotationBody = parameters[i].getAnnotation(RequestBody.class);
            String paramValue;
            if (annotationParam != null ) {
                String paramName = annotationParam.value();
                // 1. 优先从URL/Form获取
                paramValue = requestParams.get(paramName);
                // 必填校验
                // 修复点：优先检查 defaultValue
                if (paramValue == null) {
                    if (annotationParam.required() && annotationParam.defaultValue().isEmpty()) {
                        throw new IllegalArgumentException("Missing required parameter: " + paramName);
                    }
                    paramValue = annotationParam.defaultValue(); // 应用默认值
                }
                // 类型转换
                Class<?> targetType = parameters[i].getType();
                Object convertedValue = ConvertUtil.convertValue(
                        paramValue,
                        targetType,
                        annotationParam.defaultValue(),
                        parameters[i]
                );
                args[i] = convertedValue;
            } else if (annotationBody != null) {
                // 2. 如果未获取到且是@RequestBody
                if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                    args[i] = parameterSource.getBody(parameters[i].getType(), parameters[i]) ;
                    System.out.println();
//                    paramValue = (String) parameterSource.getBody(parameters[i].getType(), parameters[i]);
//                    if (paramValue == null) {
//                        throw new IllegalArgumentException("Missing required body: " + parameters[i].getName());
//                    }


                }
            }
        }
    }

    public static void process(Method method, Object[] args, HttpServletRequest request) {
        ParameterSource parameterSource = new AsyncRequestSource(request);
        // 获取参数Map（实际为Map<String, String[]>）
        Map<String, String[]> parameterMap = request.getParameterMap();

        // 转换为单值Map（取每个参数的第一个值）
        Map<String, String> singleValueMap = new HashMap<>();
        parameterMap.forEach((key, values) -> {
            if (values != null && values.length > 0) {
                singleValueMap.put(key, values[0]); // 取第一个值
            }
        });

        // 后续逻辑保持不变
        process(method, args, singleValueMap,parameterSource);
    }

//    public static void process(Method method, Object[] args, ParameterSource parameterSource) {
//        Parameter[] parameters = method.getParameters();
//        for (int i = 0; i < parameters.length; i++) {
//            RequestParam annotation = parameters[i].getAnnotation(RequestParam.class);
//            if (annotation != null) {
//                // 1. 优先从URL/Form获取
//                String[] values = parameterSource.getParameterValues(annotation.value());
//                Object paramValue = convertValues(values, method, parameters[i]);
//
//                // 2. 如果未获取到且是@RequestBody
//                if (paramValue == null && parameters[i].isAnnotationPresent(RequestBody.class)) {
//                    paramValue = parameterSource.getBody(parameters[i].getType());
//                }
//
//                args[i] = paramValue;
//            }
//        }
//    }
    /**
     * 类型转换入口（支持递归调用）
     */

}