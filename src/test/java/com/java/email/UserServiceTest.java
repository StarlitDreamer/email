package com.java.email;

import com.java.email.repository.UserRepository;
import com.java.email.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; // 假设你有一个服务类UserService来调用 getSubordinateUserIds 方法

    @Test
    public void testGetSubordinateUserIds() {
        // 测试：根据所属用户ID获取下属用户的 userId 列表
        List<String> subordinateUserIds = userService.getSubordinateUserIds("user1");
        subordinateUserIds.forEach(System.out::println);
    }
}

