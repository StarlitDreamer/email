package com.java.email.controller;


import com.java.email.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据用户ID查询下属用户的ID列表
     *
     * @param userId 用户ID
     * @return 下属用户的ID列表
     */
//    @GetMapping("/subordinate-users")
//    public Result<List<String>> getSubordinateUserIds(@RequestParam String userId) {
//        return Result.success(userService.getSubordinateUserIds(userId));
//    }

    // 返回下属用户的 userid 列表
    @GetMapping("/{userId}/subordinates/ids")
    public List<String> getSubordinateUserIds(@PathVariable String userId) {
        return userService.getSubordinateUserIds(userId);
    }
}