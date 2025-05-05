package com.t.e.test.webtest;

import com.t.e.web.RequestParam;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ErrorTest {
    public void testRequiredParam(
            @RequestParam(value = "id", required = true) String id
    ) {}

    public static void main(String[] args) throws Exception {
        Method method = ErrorTest.class.getMethod("testRequiredParam", String.class);
        Map<String, String> emptyParams = new HashMap<>();

        // 预期抛出异常：Missing required parameter: id
        TestRunner.runTest(emptyParams, method, new Object[1]);
    }
}
