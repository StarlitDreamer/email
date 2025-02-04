package org.easyarch.email.service;

import org.easyarch.email.pojo.Email;
import org.easyarch.email.pojo.EmailTask;
import org.easyarch.email.pojo.UndeliveredEmail;
import org.easyarch.email.result.ResultCodeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.easyarch.email.pojo.User;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@SpringBootTest
class EmailLogServiceTest {

    @Autowired
    private EmailLogService emailLogService;

    @Autowired
    private UserService userService;

    @Test
    void crudTest() throws IOException {
        // 日期格式解析器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 邮件任务的时间范围
        long startDate = LocalDateTime.parse("2024-12-23 09:00:00", formatter)
                .atZone(ZoneId.systemDefault()).toEpochSecond();
        long endDate = LocalDateTime.parse("2024-12-24 18:00:00", formatter)
                .atZone(ZoneId.systemDefault()).toEpochSecond();

        // 创建邮件任务
        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(UUID.randomUUID().toString());
        emailTask.setEmailTypeId("d9e90f04-0dd1-42a6-9753-a95097e3cc86");
        emailTask.setSubject("生日宴请，宾朋满座");
        emailTask.setEmailContent("生日庆祝，大家一起来玩，一块刷耍！");
        emailTask.setTaskType(3); // 节日发送
        emailTask.setStartDate(startDate);
        emailTask.setEndDate(endDate);
        emailTask.setCreatedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());
        emailTask.setBounceAmount(0L);
        emailTask.setUnsubscribeAmount(2L);
        emailTask.setReceiverId(new String[]{"zhangsan@example.com", "wangwu@example.com"});
        emailTask.setSenderId(new String[]{"lisi@example.com"});
        emailTask.setTemplateId(null);


        emailLogService.saveEmailTask(emailTask);





    }
    @Test
    public void T1() throws IOException {
        // 示例用户：张三
        User user1 = new User(
                UUID.randomUUID().toString(),   // belongUserid
                "2025-01-14T10:00:00",         // createdAt
                UUID.randomUUID().toString(),   // creatorid
                1,                             // status
                "2025-01-14T12:00:00",         // updatedAt
                "zhangsan",                    // userAccount
                new String[]{"auth1", "auth2"},// userAuthid
                "zhangsan@example.com",        // userEmail
                "emailcode123",                // userEmailCode
                "U0001",   // userid
                "张三",                         // userName
                "5d41402abc4b2a76b9719d911017c592", // userPassword (MD5加密: "hello")
                4                              // userRole
        );

        // 示例用户：李四
        User user2 = new User(
                UUID.randomUUID().toString(),   // belongUserid
                "2025-01-14T11:00:00",         // createdAt
                UUID.randomUUID().toString(),   // creatorid
                2,                             // status
                "2025-01-14T13:00:00",         // updatedAt
                "lisi",                        // userAccount
                new String[]{"auth3"},         // userAuthid
                "lisi@example.com",            // userEmail
                "emailcode456",                // userEmailCode
                "U0002",   // userid
                "李四",                         // userName
                "5f4dcc3b5aa765d61d8327deb882cf99", // userPassword (MD5加密: "password")
                3                              // userRole
        );
        userService.saveUser(user1);
        userService.saveUser(user2);

    }
    @Test
    public void T2() throws IOException {
        // 日期格式解析器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 邮件任务的时间范围
        long startDate = LocalDateTime.parse("2024-12-23 09:00:00", formatter)
                .atZone(ZoneId.systemDefault()).toEpochSecond();
        long endDate = LocalDateTime.parse("2024-12-24 18:00:00", formatter)
                .atZone(ZoneId.systemDefault()).toEpochSecond();
        UndeliveredEmail undeliveredEmail=new UndeliveredEmail();
        undeliveredEmail.setEmailId(UUID.randomUUID().toString());
        undeliveredEmail.setEmailTaskId("42fdbaeb-37f5-41ee-946b-becef7040fcb");
        undeliveredEmail.setErrorCode(6);
        undeliveredEmail.setErrorMsg("未送达");

        undeliveredEmail.setStartDate(startDate);
        undeliveredEmail.setEndDate(endDate);
        undeliveredEmail.setSenderId(new String[]{"lisi@example.com"});
        undeliveredEmail.setReceiverId(new String[]{"wangwu@example.com"});
        emailLogService.saveUndeliveredEmail(undeliveredEmail);
    }
}


//    @Test
//    public void test1(){
//        // 测试查询
//        Optional<EmailLog> foundLog = emailLogService.findById("test-2");
//        assertThat(foundLog).isPresent();
//        assertThat(foundLog.get().getMe()).isEqualTo("test@example.com");
//        EmailLog emailLog = foundLog.orElse(new EmailLog());
//        System.out.println(emailLog);
//        System.out.println(emailLog.getResultCodeEnum().getMessage());
//
//    }
//    @Test void test2(){
//        // 测试删除
//        emailLogService.deleteById("test-1");
//        Optional<EmailLog> deletedLog = emailLogService.findById("test-1");
//        assertThat(deletedLog).isEmpty();
//    }
//    @Test void test3(){
//        EmailLog emailLog = EmailLog.builder()
//                .id("test-1")
//                .subject("测试邮件")
//                .resultCodeEnum(ResultCodeEnum.SUCCESS)
//                .logtoList(List.of(new Logto("test@example.com",ResultCodeEnum.FAIL),new Logto("test@example.com",ResultCodeEnum.FAIL)))
//                .sendTime(LocalDateTime.now().toString())
//                .build();
//        // 测试更新
//        emailLog.setSubject("更新后的主题");
//        EmailLog updatedLog = emailLogService.update(emailLog);
//        assertThat(updatedLog.getSubject()).isEqualTo("更新后的主题");
//
//        // 测试条件查询
//        List<EmailLog> logs = emailLogService.findByMe("test@example.com");
//        assertThat(logs).hasSize(1);
//        assertThat(logs.get(0).getSubject()).isEqualTo("更新后的主题");
//    }
//    @Test
//    public void test4() throws JsonProcessingException {
//        List<Logto> logtoList = List.of(new Logto("test@example.com",ResultCodeEnum.FAIL),new Logto("test@example.com",ResultCodeEnum.FAIL));
//
//        System.out.println(logtoList);
//    }
//}