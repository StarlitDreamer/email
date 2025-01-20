package com.java.email;

import com.java.email.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisServiceTest {

    @Autowired
    private RedisService redisService;

//    @Test
//    public void testSetAndGetKey() {
//        // 测试数据
//        String key = "recipient:898ea52b-67da-4bcb-aab6-5419d0d32299";
//        String value = "testValue";
//
//        // 设置值
//        redisService.setKey(key, value);
//
//        // 获取值
//        String retrievedValue = redisService.getKey(key);
//        System.out.println(retrievedValue);
//        // 断言
//        assertEquals(value, retrievedValue, "获取的值应与设置的值一致");
//
//        // 清理测试数据
//        redisService.deleteKey(key); // 使用 deleteKey 方法删除键
//    }

    @Test
    public void testSetAndGetKey() {
        // 测试数据
        String key = "recipient:bcea3e7f-124b-4b27-a34f-ba8a40090cdf";
//        String value = "testValue";

        // 设置值
//        redisService.setKey(key, value);

        // 获取值
        String retrievedValue = redisService.getKey(key);
        System.out.println(retrievedValue);
        // 断言
//        assertEquals(value, retrievedValue, "获取的值应与设置的值一致");

        // 清理测试数据
//        redisService.deleteKey(key); // 使用 deleteKey 方法删除键
    }

//    recipient:cf0115f6-0185-4854-a21c-e59705d93d8d

    @Test
    public void testKeyNotExists() {
        String key = "nonExistentKey";
        String value = redisService.getKey(key);
        assertNull(value, "不存在的键应返回 null");
    }

    @Test
    public void testDeleteKey() {
        String key = "testKey";
        String value = "testValue";

        // 设置值
        redisService.setKey(key, value);

        // 删除键
        redisService.deleteKey(key);

        // 验证键是否已删除
        String retrievedValue = redisService.getKey(key);
        assertNull(retrievedValue, "删除键后应返回 null");
    }
}
/*{
    "subject": "圣诞节快乐",
    "template_id": "template_uuid",
    "start_date": 1737025815,
    "receiver": [
        {
            "receiver_id": "receiver_uuid"
        },
        {
            "receiver_id": "receiver_uuid"
        }
    ],
    "attachment": [
        {
            "attachment_id": "attachment_id",
            "attachment_url": "attachment_url"
        },
        {
            "attachment_id": "attachment_id",
            "attachment_url": "attachment_url"
        }
    ]
}
* */