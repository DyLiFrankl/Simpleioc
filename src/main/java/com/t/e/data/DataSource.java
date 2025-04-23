package com.t.e.data;

import com.t.e.simpleioc.annotations.Component;

import java.sql.Connection;
import java.sql.SQLException;

// 1. 数据源抽象（支持后续扩展连接池）

public interface DataSource {
    Connection getConnection() throws SQLException;
}