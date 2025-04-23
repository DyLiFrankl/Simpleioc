package com.t.e.data;

import com.t.e.simpleioc.annotations.Component;
import com.t.e.util.PropertyUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class SimpleDataSource implements DataSource {
    private String url;
    private String username;
    private String password;

    // 通过配置类或properties文件注入参数
    public SimpleDataSource() {
        try {
            // 修复1：确保驱动类加载
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL driver", e);
        }
        this.url = PropertyUtils.get("jdbc.url");
        this.username = PropertyUtils.get("jdbc.username");
        this.password = PropertyUtils.get("jdbc.password");
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(url, username, password);
        if (c == null) {
            throw new SQLException();
        }
        return DriverManager.getConnection(url, username, password);
    }
}