package com.t.e.data;

// 自定义运行时异常（继承 RuntimeException）
public class DataAccessException extends RuntimeException {
    public DataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
