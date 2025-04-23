package com.t.e.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
//用于获取构造函数的参数名
public class SimpleParameterNameDiscoverer {
    public String[] getParameterNames(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        String[] paramNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            paramNames[i] = parameters[i].getName();
        }
        return paramNames;
    }
}
