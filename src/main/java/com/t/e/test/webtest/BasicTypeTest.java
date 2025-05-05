package com.t.e.test.webtest;

import com.t.e.web.RequestParam;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BasicTypeTest {
    public void testBasic(
            @RequestParam ("name") String name1,
            @RequestParam("age") int age1,
            @RequestParam("active") boolean active1
    ) {}

    public static void main(String[] args) throws Exception {
        Method method = BasicTypeTest.class.getMethod("testBasic", String.class, int.class, boolean.class);
        Map<String, String> params = new HashMap<>();
        params.put("name", "Alice");
        params.put("age", "25");
        params.put("active", "true");

        TestRunner.runTest(params, method, new Object[3]);
    }
}