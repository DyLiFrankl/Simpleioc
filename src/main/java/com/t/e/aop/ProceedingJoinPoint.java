package com.t.e.aop;

import java.lang.reflect.Method;

public class ProceedingJoinPoint {
    private final Object target;    // 目标对象
    private final Method method;    // 目标方法
    private final Object[] args;    // 方法参数

    public ProceedingJoinPoint(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    // 执行目标方法
    public Object proceed() throws Throwable {
        return method.invoke(target, args);
    }

    // 获取方法名
    public String getMethodName() {
        return method.getName();
    }

    // 获取参数
    public Object[] getArgs() {
        return args;
    }
}
