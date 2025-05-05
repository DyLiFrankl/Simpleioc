package com.t.e.web;

import java.lang.reflect.Parameter;

public interface ParameterSource {
    // 获取单值（如URL参数）
    String getParameter(String name);

    // 获取多值（如复选框）
    String[] getParameterValues(String name);

    // 获取body数据（如JSON）
    <T> T getBody(Class<T> bodyType, Parameter parameter);
}
