package com.t.e.web;

import com.t.e.simpleioc.annotations.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Controller.java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component // 标记为 Bean
public @interface Controller {
}