package com.t.e.condition;

import com.t.e.simpleioc.SimpleIoC;

import java.lang.annotation.Annotation;

public class SimpleConditionContext {
    private final ClassLoader classLoader;
    private final Class<?> targetClass;
    private final SimpleIoC container; // 新增：容器引用

    // 构造函数需要三个参数
    public SimpleConditionContext(
            ClassLoader classLoader,
            Class<?> targetClass,
            SimpleIoC container
    ) {
        this.classLoader = classLoader;
        this.targetClass = targetClass;
        this.container = container;
    }

    // 检查 Bean 是否存在
    public boolean containsBean(Class<?> beanType) {
        try {
            container.getBean(beanType); // 尝试获取 Bean
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 获取类上的指定注解
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return targetClass.getAnnotation(annotationType);
    }

    // 检查类是否存在
    public boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
