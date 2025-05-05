package com.t.e.test.webtest;

import com.t.e.test.webtest.entity.User;
import com.t.e.web.RequestParam;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionTest {
    public void testList(
            @RequestParam("ids") List<Integer> ids,
            @RequestParam("users") List<User> users
    ) {}

    public static void main(String[] args) throws Exception {
        Method method = CollectionTest.class.getMethod("testList", List.class, List.class);
        Map<String, String> params = new HashMap<>();
        params.put("ids", "1,2,3");
        params.put("users", "[{\"name\":\"Bob\",\"age\":30},{\"name\":\"Charlie\"}]");

        TestRunner.runTest(params, method, new Object[2]);
    }
}
