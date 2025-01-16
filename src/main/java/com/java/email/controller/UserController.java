package com.java.email.controller;


import com.java.email.common.Result;
import com.java.email.entity.User;
import com.java.email.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 创建用户
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        return Result.success(userService.createUser(user));
    }

    // 根据 ID 查询用户
    @GetMapping("/{id}")
    public Result<Optional<User>> getUserById(@PathVariable String id) {
        Optional<User> userById = userService.getUserById(id);
        return Result.success(userById);
    }

    // 更新用户
    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User userDetails) {
        return userService.updateUser(id, userDetails);
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return Result.success();
    }

    // 查询所有用户
    @GetMapping
    public Result<Iterable<User>> getAllUsers() {
        Iterable<User> allUsers = userService.getAllUsers();
        return Result.success(allUsers);
    }
}