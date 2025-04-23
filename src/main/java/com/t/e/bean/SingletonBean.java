package com.t.e.bean;

import com.t.e.simpleioc.annotations.Autowired;
import com.t.e.simpleioc.annotations.Component;
import com.t.e.simpleioc.annotations.Scope;
import jakarta.enterprise.inject.spi.Bean;

// SingletonBean.java
@Component
@Scope("singleton") // 默认值，可省略
public class SingletonBean {

    private PrototypeBean prototypeBean;

    @Autowired
    public SingletonBean(PrototypeBean prototypeBean) {
        System.out.println("SingletonBean created!");
        this.prototypeBean = prototypeBean;
    }
}

