package com.t.e.util;

public class AssertUtils {
    // 基础 assertNotNull
    public static void assertNotNull(Object object) {
        assertNotNull(object, "Expected not null, but was null");
    }

    // 带自定义错误信息的 assertNotNull
    public static void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new AssertionError(message != null ? message : "Expected not null, but was null");
        }
    }
}
