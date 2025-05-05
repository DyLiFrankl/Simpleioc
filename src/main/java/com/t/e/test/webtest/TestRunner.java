package com.t.e.test.webtest;

import com.t.e.web.MyParamProcessor;

import java.lang.reflect.Method;
import java.util.*;

public class TestRunner {
    /**
     * 运行单个测试用例
     * @param params 模拟的请求参数（Map格式）
     * @param method 要测试的目标方法
     * @param args   方法参数初始数组（长度需匹配）
     */
    public static void runTest(Map<String, String> params, Method method, Object[] args) {
        try {
            System.out.println("\n=== 测试方法: " + method.getName() + " ===");
            System.out.println("输入参数: " + params);

            // 执行参数绑定
//            MyParamProcessor.process(method, args, params);

            // 打印绑定结果
            System.out.println("绑定结果:");
            for (int i = 0; i < args.length; i++) {
                System.out.printf("参数%d [%s]: %s\n",
                        i,
                        method.getParameters()[i].getType().getSimpleName(),
                        args[i]
                );
            }
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}