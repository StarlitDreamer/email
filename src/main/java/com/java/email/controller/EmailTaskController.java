//package com.java.email.controller;
//
//import com.java.email.dto.CreateCycleEmailTaskRequest;
//import com.java.email.dto.CreateEmailTaskRequest;
//import com.java.email.entity.EmailTask;
//import com.java.email.service.EmailTaskService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/email-tasks")
//public class EmailTaskController {
//
//    @Autowired
//    private EmailTaskService emailTaskService;
//
//    /**
//     * 创建普通邮件任务
//     */
//    @PostMapping("create")
//    public String createEmailTask(@RequestBody CreateEmailTaskRequest request) {
//        System.out.println(request.getSenderId());
//        return emailTaskService.createEmailTask(request);
//    }
//
//    /**
//     * 创建循环邮件任务
//     */
//    @PostMapping("createCycle")
//    public String createCycleEmailTask(@RequestBody CreateCycleEmailTaskRequest request) {
//        return emailTaskService.createCycleEmailTask(request);
//    }
//
//    /**
//     * 创建节日邮件任务
//     */
//    @PostMapping("createFestival")
//    public String createFestivalEmailTask(@RequestBody EmailTask request) {
//        return emailTaskService.createFestivalEmailTask(request);
//    }
//}