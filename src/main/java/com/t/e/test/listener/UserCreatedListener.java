package com.t.e.test.listener;

import com.t.e.listener.ApplicationListener;
import com.t.e.simpleioc.annotations.Component;

@Component
public class UserCreatedListener implements ApplicationListener<UserCreatedEvent> {
    @Override
    public void onApplicationEvent(UserCreatedEvent event) {
        System.out.println("[publish] User created: " + event.getUsername());
    }
}
