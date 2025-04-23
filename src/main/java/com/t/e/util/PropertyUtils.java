package com.t.e.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {
    private static final Properties props = new Properties();

    // 静态加载配置文件（容器启动时调用）
    public static void load(String fileName) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(fileName)) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties: " + fileName, e);
        }
    }

    // 获取配置值
    public static String get(String key) {
        return props.getProperty(key);
    }
}
