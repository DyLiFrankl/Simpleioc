package com.t.e.listener;

// 事件监听器接口
public interface ApplicationListener<E extends ApplicationEvent> {
    void onApplicationEvent(E event);
}

