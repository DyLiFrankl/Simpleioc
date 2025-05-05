package com.t.e.web;

import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Parameter;

public class ServletRequestSource implements ParameterSource {
    private final HttpServletRequest request;

    public ServletRequestSource(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    @Override
    public <T> T getBody(Class<T> bodyType, Parameter parameter) {
        try {
            // 使用Jackson解析JSON body
            return new ObjectMapper().readValue(request.getInputStream(), bodyType);
        } catch (IOException e) {
            throw new RuntimeException("Body解析失败", e);
        }
    }
}
