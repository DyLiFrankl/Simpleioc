package com.t.e.simpleioc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.CONSTRUCTOR, ElementType.FIELD}) // 支持构造函数和字段
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {

}