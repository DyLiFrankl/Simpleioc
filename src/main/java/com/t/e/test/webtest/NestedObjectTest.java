package com.t.e.test.webtest;

import com.t.e.test.webtest.entity.User;
import com.t.e.web.RequestParam;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class NestedObjectTest {
    public void testNested(
            @RequestParam("user") User user,
            @RequestParam("config") Map<String, String> config
    ) {}

    public static void main(String[] args) throws Exception {
        Method method = NestedObjectTest.class.getMethod("testNested", User.class, Map.class);
        Map<String, String> params = new HashMap<>();
        params.put("user", "name=Alice&age=25&address.city=Beijing");
        params.put("config", "{\"key1\":\"value1\",\"key2\":\"value2\"}");

        TestRunner.runTest(params, method, new Object[2]);
    }
}

