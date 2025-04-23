package com.t.e.test.listener;

import com.t.e.listener.ApplicationEvent;

//定义自定义事件
public class UserCreatedEvent extends ApplicationEvent {
    private final String username;

    public UserCreatedEvent(Object source, String username) {
        super(source);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
