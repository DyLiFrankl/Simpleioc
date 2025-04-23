package com.t.e.test.listener;

import com.t.e.listener.EventListener;
import com.t.e.simpleioc.annotations.Component;

@Component
public class LogService {
    @EventListener(UserCreatedEvent.class)
    public void logUserCreation(UserCreatedEvent event) {
        System.out.println("[Log] User created: " + event.getUsername());
    }
}
