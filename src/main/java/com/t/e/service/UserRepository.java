package com.t.e.service;

import com.t.e.simpleioc.annotations.Autowired;
import com.t.e.simpleioc.annotations.Component;
import com.t.e.simpleioc.annotations.Scope;

@Scope(value = "prototype")
@Component
public class UserRepository {

//    @Autowired
//    private UserService userService;

    private  UserService userService;

    @Autowired
    public UserRepository(UserService userService) {
        this.userService = userService;
    }

    private String username = "John Doe";

    public UserRepository(){

    }

    public String getUserName(){
        return this.username;
    }
}
