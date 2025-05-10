package com.t.e.util;


import com.zaxxer.hikari.HikariConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {
    private static final Properties props = new Properties();
    private static final HikariConfig config = new HikariConfig();
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

    public static void setConfig() {
        config.setJdbcUrl(PropertyUtils.get("jdbc.url"));
        config.setUsername(PropertyUtils.get("jdbc.username"));
        config.setPassword(PropertyUtils.get("jdbc.password"));
        config.setMaximumPoolSize(Integer.parseInt(PropertyUtils.get("MaximumPoolSize"))); // 最大连接数（根据数据库配置调整）
        config.setMinimumIdle(Integer.parseInt(PropertyUtils.get("MinimumIdle")));
        config.setConnectionTimeout(Integer.parseInt(PropertyUtils.get("ConnectionTimeout"))); // 30秒超时
    }

    public static HikariConfig getHikariConfig() {
        setConfig();
        return config;
    }
}
