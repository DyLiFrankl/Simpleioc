package com.t.e.listener;

public abstract class ApplicationEvent {
    private final Object source; // 事件源（可选）
    private final long timestamp;

    public ApplicationEvent(Object source) {
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Object getSource() {
        return source;
    }
}
