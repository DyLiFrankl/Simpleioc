package com.t.e.service;

import com.t.e.aop.Loggable;
import com.t.e.condition.ConditionalOnClass;
import com.t.e.condition.ConditionalOnMissingBean;
import com.t.e.init.PostConstruct;
import com.t.e.simpleioc.annotations.Autowired;
import com.t.e.simpleioc.annotations.Component;
import com.t.e.simpleioc.annotations.Scope;

import javax.sql.DataSource;

//@ConditionalOnMissingBean(UserRepository.class)
@Scope
@Component
public class UserService {
//    @Autowired
//    private UserRepository userRepository;

    private  UserRepository userRepository;

    // 构造器注入
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //cglib 需要无参构造函数
    public UserService(){}

    @PostConstruct
    public void init() {
        System.out.println("UserService initialized!");
    }

    public void printUserName(){
        System.out.println("User:" + userRepository.getUserName());
    }

    public void throwException() { throw new RuntimeException("Test Exception"); }

    public String getUserName() {
        return userRepository.getUserName();
    }
}
