package com.java.email.service;

import com.java.email.common.Redis.RedisService;
import com.java.email.constant.MagicMathConstData;
import com.java.email.constant.RedisConstData;
import com.java.email.pojo.Customer;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.java.email.pojo.User;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



@SpringBootTest
class EmailLogServiceTest {

    @Autowired
    private EmailLogService emailLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomerService customerService;

    @Test
    void tokenTest() {


        // 2. 准备token数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("id","123");
        claims.put("role", 3);
        claims.put("name", "小管理4");

        // 3. 生成token
        String token = JwtUtil.genToken(claims);

        // 4. 存储到Redis（用于token验证）
        String redisKey = RedisConstData.USER_LOGIN_TOKEN + "123";
        redisService.set(redisKey, token);
        System.out.println("token: " + token);


    }
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
        emailTask.setSubject("生sfsadffsa 起哈哈哈");
        emailTask.setEmailContent("生日快gsaa嘎尔哥块刷耍！");
        emailTask.setTaskType(3); // 节日发送
        emailTask.setStartDate(startDate);
        emailTask.setEndDate(endDate);
        emailTask.setCreatedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond());
        emailTask.setBounceAmount(1);
        emailTask.setUnsubscribeAmount(0);
        emailTask.setReceiverId(new String[]{"zhaoliu@example.com"});
        emailTask.setSenderId("wangwu@example.com");
        emailTask.setTemplateId(null);
        emailTask.setSenderName("王五");
        emailTask.setReceiverName(new String[]{"赵六"});



        emailLogService.saveEmailTask(emailTask);





    }
    @Test
    public void T1() throws IOException {
        // 示例用户：张三
//        User user1 = new User(
//                "071f17d9-00dd-4448-8c8c-3039ab8102aa",   // belongUserid
//                "2025-01-14T10:00:00",         // createdAt
//                UUID.randomUUID().toString(),   // creatorid
//                2,                             // status
//                "2025-01-14T12:00:00",         // updatedAt
//                "wangwu",                    // userAccount
//                new String[]{"auth1", "auth2"},// userAuthid
//                "wangwu@example.com",        // userEmail
//                "emailcode123",                // userEmailCode
//                "U0003",   // userid
//                "王五",                         // userName
//                "5d41402abc4b2a76b9719d911017c592", // userPassword (MD5加密: "hello")
//                4                              // userRole
//        );
//
//        // 示例用户：李四
//        User user2 = new User(
//                "071f17d9-00dd-4448-8c8c-3039ab8102aa",   // belongUserid
//                "2025-01-14T11:00:00",         // createdAt
//                UUID.randomUUID().toString(),   // creatorid
//                2,                             // status
//                "2025-01-14T13:00:00",         // updatedAt
//                "zhaoliu",                        // userAccount
//                new String[]{"auth3"},         // userAuthid
//                "zhaoliu@example.com",            // userEmail
//                "emailcode456",                // userEmailCode
//                "U0005",   // userid
//                "赵六",                         // userName
//                "5f4dcc3b5aa765d61d8327deb882cf99", // userPassword (MD5加密: "password")
//                4                              // userRole
//        );
//        userService.saveUser(user1);
//        userService.saveUser(user2);

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
        undeliveredEmail.setEmailTaskId("5cc0ec87-8a9c-4529-8477-efec7b0eb9ed");
        undeliveredEmail.setErrorCode(200);
        undeliveredEmail.setErrorMsg("无");

        undeliveredEmail.setStartDate(startDate);
        undeliveredEmail.setEndDate(endDate);
        undeliveredEmail.setSenderId("zhangsan@example.com");
        undeliveredEmail.setReceiverId("zhaoliu@example.com");
        undeliveredEmail.setSenderName("张三");
        undeliveredEmail.setReceiverName("赵六");

        emailService.saveEmailTask(undeliveredEmail);
    }
    @Test
    public void T3() throws IOException {
        Customer customer = new Customer();
        customer.setBirth("2023-01-02");
        customer.setBelongUserid(null);
        customer.setCustomerid(UUID.randomUUID().toString());
        customer.setCustomerName("李四");
        customer.setCustomerLevel(3L);
        customer.setEmails(new String[]{"wangwu@example.com"});
        customer.setSex("男");
        customer.setStatus(3L);
        customerService.saveCustomer(customer);


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