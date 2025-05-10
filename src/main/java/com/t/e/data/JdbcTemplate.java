package com.t.e.data;

import com.t.e.simpleioc.annotations.Autowired;
import com.t.e.simpleioc.annotations.Component;
import com.t.e.util.PropertyUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTemplate {
    @Autowired
    private SimpleDataSource dataSource;

    private final HikariDataSource hikariDataSource = new HikariDataSource(PropertyUtils.getHikariConfig());

    // 查询单条记录
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rowMapper, args).stream().findFirst().orElse(null);
    }

    // 查询多条记录
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = hikariDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(true);
            setParameters(ps, args);
            ResultSet rs = ps.executeQuery();

            List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // 更新操作（INSERT/UPDATE/DELETE）
    public int update(String sql, Object... args) {
        try (Connection conn = hikariDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, args);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sql, e);
        }
    }

    // 批处理操作
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        try (Connection conn = hikariDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Object[] args : batchArgs) {
                setParameters(ps, args);
                ps.addBatch();
            }
            return ps.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Batch update failed: " + sql, e);
        }
    }

    // 设置 PreparedStatement 参数
    private void setParameters(PreparedStatement ps, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof java.util.Date) {
                // 处理日期类型转换
                java.util.Date date = (java.util.Date) arg;
                ps.setTimestamp(i + 1, new Timestamp(date.getTime()));
            } else {
                ps.setObject(i + 1, arg);
            }
        }
    }
}



