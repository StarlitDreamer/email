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
        emailTask.setSubject("圣诞节优惠主题");
        emailTask.setEmailContent("圣诞节即将来临，享受限时优惠！");
        emailTask.setTaskType(3); // 节日发送
        emailTask.setTaskStatus(1); // 发送中
        emailTask.setOperateStatus(1); // 开始态
        emailTask.setStartDate(startDate);
        emailTask.setEndDate(endDate);
        emailTask.setCreatedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());
        emailTask.setBounceAmount(0L);
        emailTask.setUnsubscribeAmount(0L);
        emailTask.setReceiverId(new String[]{"U0002", "U0003", "U0004"});
        emailTask.setSenderId(new String[]{"U0001"});
        emailTask.setTemplateId(null);

        emailLogService.saveEmailTask(emailTask);

        // 创建该任务下的几封邮件
        List<Email> emails = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Email email = Email.builder()
                    .emailId(UUID.randomUUID().toString())
                    .emailTaskId(emailTask.getEmailTaskId())
                    .emailStatus(i) // 1:已送达, 2:已打开, 3:未送达
                    .receiverId(new String[]{"U000" + (i+1)})
                    .senderId(new String[]{"U0001"})
                    .createdAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond())
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            // 设置错误信息，仅当状态为未送达时填写
            if (email.getEmailStatus() == 3) {
                email.setErrorCode(404);
                email.setErrorMsg("邮箱地址不存在");
            }

            emails.add(email);
            emailLogService.saveEmail(email);
        }


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
        undeliveredEmail.setEmailTaskId("");
        undeliveredEmail.setErrorCode(404);
        undeliveredEmail.setErrorMsg("网络波动");
        undeliveredEmail.setResendCode(null);
        undeliveredEmail.setResendMsg(null);
        undeliveredEmail.setResendStatus(1);
        undeliveredEmail.setResendStartDate(null);
        undeliveredEmail.setResendEndDate(null);
        undeliveredEmail.setCreatedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());
        undeliveredEmail.setStartDate(startDate);
        undeliveredEmail.setEndDate(endDate);
        undeliveredEmail.setSenderId(new String[]{"U0002"});
        undeliveredEmail.setReceiverId(new String[]{"U0003"});
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