package com.t.e.web;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    String value() default "";      // 参数名（如 "id"）
    boolean required() default true; // 是否必填
    String defaultValue() default ""; // 默认值
}

