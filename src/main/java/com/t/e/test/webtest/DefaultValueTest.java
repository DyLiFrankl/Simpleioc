package com.t.e.test.webtest;

import com.t.e.web.RequestParam;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DefaultValueTest {
    public void testDefault(
            @RequestParam(value = "role", defaultValue = "guest") String role
    ) {}

    public static void main(String[] args) throws Exception {
        Method method = DefaultValueTest.class.getMethod("testDefault", String.class);
        Map<String, String> emptyParams = new HashMap<>();

        // 预期输出：role=guest
        TestRunner.runTest(emptyParams, method, new Object[1]);
    }
}

