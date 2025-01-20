package com.java.email;

import com.java.email.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest // 加载完整的 Spring 上下文
public class UserServiceIntegrationTest {

//    @Autowired
//    private YourClass yourClass; // 替换为包含 getSubordinateUserIds 方法的类

    @MockBean // 使用 Spring Boot 的 MockBean 模拟 userService
    private UserService userService;

    @Test
    public void testGetSubordinateUserIds() {
        String userId = "user123";
        List<String> subordinateUserIds = userService.getSubordinateUserIds(userId);

        System.out.println("11");
        for (String subUserId : subordinateUserIds) {
            System.out.println("11");
            System.out.println(subUserId);
        }
//        // 模拟输入
//        String userId = "123";
//        List<String> expectedSubordinateIds = Arrays.asList("456", "789");
//
//        // 模拟 userService 的行为
//        when(userService.getSubordinateUserIds(userId)).thenReturn(expectedSubordinateIds);
//
//        // 调用被测试的方法
//        List<String> actualSubordinateIds = yourClass.getSubordinateUserIds(userId);
//
//        // 验证结果
//        assertEquals(expectedSubordinateIds, actualSubordinateIds);
    }
}
