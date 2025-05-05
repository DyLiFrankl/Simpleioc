package com.t.e.web;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

// 处理器映射封装类
public class HandlerMapping {
    private Object controller;
    private Method method;

    public HandlerMapping(Object controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    public boolean supportsMethod(String httpMethod) {
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        return mapping.method().equalsIgnoreCase(httpMethod);
    }

    // 新增方法：返回当前处理的 Method 对象
    public Method getMethod() {
        return method;
    }

    public Object invoke(HttpServletRequest request) throws Exception {
        // 参数绑定
        Object[] args = new Object[method.getParameterCount()];

        MyParamProcessor.process(method,args,request);
//        for (int i = 0; i < args.length; i++) {
//            Class<?> paramType = method.getParameterTypes()[i];
//            if (paramType == HttpServletRequest.class) {
//                args[i] = request;
//            } else {
//                String name = method.getParameters()[i].getName();
//                String paramValue = request.getParameter(name);
//                args[i] = paramValue;
//            }
//        }
        return method.invoke(controller, args);
    }
}