package com.java.email.controller;

import com.java.email.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 根据 userId 查询 userEmail
    @GetMapping("/get-email")
    public String getUserEmail(@RequestParam String userId) {
        return userService.getUserEmailByUserId(userId);
    }

    //    @GetMapping("/getUserIdByEmail")
//    public String getUserIdByEmail(@RequestParam String userEmail) {
//        return userService.getUserIdByEmail(userEmail);
//    }
// 根据用户邮箱查找用户名
    @GetMapping("/getUserName")
    public String getUserNameByEmail(@RequestParam String userEmail) {
        return userService.getUserNameByEmail(userEmail);
    }
}