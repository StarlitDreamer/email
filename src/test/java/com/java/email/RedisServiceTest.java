package com.java.email;

import com.java.email.entity.Supplier;
import com.java.email.service.RedisService;
import com.java.email.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SupplierService supplierService;

//    @Test
//    public void testGetSuppliersFromRedis() {
//        // 替换为实际的 Redis Key
//        String redisKey = "supplier:search:b1e9c1f3-6883-424d-ad66-70f9a59cc29d";
//
//        // 调用方法获取数据
//        List<Supplier> suppliers = supplierService.getSuppliersFromRedis(redisKey);
//
//        // 断言结果不为空
//        assertNotNull(suppliers, "Suppliers list should not be null");
//        assertFalse(suppliers.isEmpty(), "Suppliers list should not be empty");
//
//        // 打印反序列化结果
//        for (Supplier supplier : suppliers) {
//            System.out.println("Supplier Name: " + supplier.getSupplierName());
//            System.out.println("Emails: " + supplier.getEmails());
//
//        }
//    }

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
        String key = "customer:search:9617e3ae-f9ae-4e7e-a042-5be625fc9b0d";
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