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
    public Result<String> getUserInfo() {
        return Result.Builder(200,"sucess",userService.getUserName()); // 返回 "John Doe"
    }

    @RequestMapping(value = "/data", method = "GET")
    @ResponseBody
    public Result<List<Category>> getData() {
//        userService.printUserName();
        List<Category> categoryList = jdbcTemplate.query(
                "SELECT * FROM category WHERE status > ? ",
                new BeanPropertyRowMapper<>(Category.class),
                0
        );
        return Result.Builder(200,"sucess",categoryList);
    }

    @RequestMapping(value = "/data/one", method = "GET")
    @ResponseBody
    public Result<Category> getOneData() {
//        userService.printUserName();
                Category category = jdbcTemplate.queryForObject(
                "SELECT * FROM category WHERE id = ?",
                new BeanPropertyRowMapper<>(Category.class),
                1
        );
        return Result.Builder(200,"sucess",category);
    }

    @RequestMapping(value = "/data/update", method = "GET")
    @ResponseBody
    public Result<Category> updateOneData(@RequestBody Category category) {
//        userService.printUserName();
         Integer status = jdbcTemplate.update(
                "UPDATE category SET name =?, image_url= ?,parent_id=? WHERE id = ?",
                 "数码办公abc",
                 "https//......",
                 12,
                 1
        );
        return getOneData();
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
    public User test(@RequestBody User user, @RequestParam("id")Integer id) {
        User user1 = new User();
        user1.setAge(user.getAge());
        user1.setName(user.getName());
        user1.setId(id);
        return user1;
    }
//    public String test(@RequestParam(value = "id")Integer id, @RequestParam(value = "name",required = false)String name) {
//        return id.toString()+name;
//    }
}
