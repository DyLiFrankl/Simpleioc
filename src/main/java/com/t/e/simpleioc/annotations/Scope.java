package com.t.e.simpleioc.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 作用于类
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
public @interface Scope {
    String value() default "singleton"; // 默认值为单例
}