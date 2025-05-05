package com.t.e.controller;

import com.t.e.data.BeanPropertyRowMapper;
import com.t.e.data.JdbcTemplate;
import com.t.e.service.UserService;
import com.t.e.simpleioc.annotations.Autowired;
import com.t.e.simpleioc.annotations.Component;
import com.t.e.web.*;
import generator.domain.Category;

import java.util.List;


@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public UserController(){}

    @RequestMapping(value = "/info", method = "GET")
    @ResponseBody
    public String getUserInfo() {
        return userService.getUserName(); // 返回 "John Doe"
    }

    @RequestMapping(value = "/data", method = "GET")
    @ResponseBody
    public List<Category> getData() {
//        userService.printUserName();
        List<Category> categoryList = jdbcTemplate.query(
                "SELECT * FROM category WHERE status > ?",
                new BeanPropertyRowMapper<>(Category.class),
                0
        );
        return categoryList;
    }

    @RequestMapping(value = "/home", method = "GET")
    public String homePage() {
        return "success"; // 返回视图名（对应 success.html）
    }

    @RequestMapping(value = "/test", method = "GET")
    @ResponseBody
    public String test() {
        return "test"; // 返回视图名（对应 success.html）
    }

    @RequestMapping(value = "/test1", method = "POST")
    @ResponseBody
    public User test(@RequestBody User user) {
        return user;
    }
//    public String test(@RequestParam(value = "id")Integer id, @RequestParam(value = "name",required = false)String name) {
//        return id.toString()+name;
//    }
}
