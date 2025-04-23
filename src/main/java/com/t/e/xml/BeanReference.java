package com.t.e.xml;

// 用于表示 Bean 引用（如 <property name="userRepository" ref="userRepository"/>）
public class BeanReference {
    private final String beanId;
    public BeanReference(String beanId) { this.beanId = beanId; }
    public String getBeanId() { return beanId; }
}
