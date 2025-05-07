package com.t.e.web;

import com.mysql.cj.util.StringUtils;
import com.t.e.util.ConvertUtil;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;


public class MyParamProcessor {
    /**
     * 核心方法：处理方法的参数绑定
     * @param method 目标方法
     * @param args 参数数组（会被修改）
     * @param parameterSource 请求参数封装
     */
    public static void process(Method method, Object[] args, ParameterSource parameterSource) throws IOException, RuntimeException {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            RequestParam annotationParam = parameters[i].getAnnotation(RequestParam.class);
            RequestBody annotationBody = parameters[i].getAnnotation(RequestBody.class);
            String paramValue;
            if (annotationParam != null ) {
                String paramName = annotationParam.value();
                paramValue = parameterSource.getParameter(paramName);
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
                }
            }
        }
    }

}