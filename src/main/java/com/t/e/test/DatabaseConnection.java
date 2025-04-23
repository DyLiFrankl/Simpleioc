package com.t.e.test;

import com.t.e.init.PostConstruct;
import com.t.e.init.PreDestroy;
import com.t.e.simpleioc.annotations.Component;

@Component
public class DatabaseConnection {
    public DatabaseConnection() {
        System.out.println("DatabaseConnection created!");
    }

    @PostConstruct
    public void connect() {
        System.out.println("Database connected!");
    }

    @PreDestroy
    public void disconnect() {
        System.out.println("Database disconnected!");
    }
}
