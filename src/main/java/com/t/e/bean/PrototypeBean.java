package com.t.e.bean;

import com.t.e.simpleioc.annotations.Autowired;
import com.t.e.simpleioc.annotations.Component;
import com.t.e.simpleioc.annotations.Scope;

// PrototypeBean.java
@Component
@Scope("prototype")
public class PrototypeBean {
    @Autowired
    private SingletonBean singletonBean;

    public PrototypeBean() {
        System.out.println("PrototypeBean created!");
    }
}
