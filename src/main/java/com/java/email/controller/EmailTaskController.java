//package com.java.email.controller;
//
//import com.java.email.Dto.CreateCycleEmailTaskRequest;
//import com.java.email.Dto.CreateFestivalEmailTaskRequest;
//import com.java.email.common.Result;
//import com.java.email.entity.EmailTask;
//import com.java.email.Dto.CreateEmailTaskRequest;
//import com.java.email.Dto.UpdateBirthdayEmailTaskRequest;
//import com.java.email.service.EmailTaskService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/email-tasks")
//public class EmailTaskController {
//
//    @Autowired
//    private EmailTaskService emailTaskService;
//
//    /**
//     * 根据邮件任务 ID 修改任务操作状态
//     *
//     * @param emailTaskId   邮件任务 ID
//     * @param operateStatus 新的任务操作状态
//     * @return 操作结果
//     */
////    @PutMapping("/update-status")
////    public Result updateOperateStatus(
////            @RequestParam String emailTaskId,  // 邮件任务ID
////            @RequestParam int operateStatus,   // 操作状态
////            @RequestHeader("currentUserId") String currentUserId) { // 从请求头中获取当前用户ID
////        try {
////            // 调用服务层方法，传入当前用户ID进行权限校验
////            EmailTask updatedTask = emailTaskService.updateOperateStatus(emailTaskId, operateStatus, currentUserId);
////            return Result.success(updatedTask);
////        } catch (IllegalArgumentException e) {
////            return Result.error(e.getMessage());
////        } catch (Exception e) {
////            return Result.error("更新任务操作状态失败");
////        }
////    }
////    @PutMapping("/operate-status")
////    public Result updateOperateStatus(
////            @RequestParam String emailTaskId,  // 改为 @RequestParam
////            @RequestParam int operateStatus) {
////        try {
////            EmailTask updatedTask = emailTaskService.updateOperateStatus(emailTaskId, operateStatus);
////            return Result.success(updatedTask);
////        } catch (IllegalArgumentException e) {
////            return Result.error(e.getMessage());
////        } catch (Exception e) {
////            return Result.error("更新任务操作状态失败");
////        }
////    }
//
//    /**
//     * 更新邮件任务的操作状态
//     *
//     * @param emailTaskId   邮件任务ID
//     * @param operateStatus 新的操作状态
//     * @return 更新结果
//     */
//    @PutMapping("/reset-status")
//    public Result resetOperateStatus(
//            @RequestParam String emailTaskId,  // 邮件任务ID
//            @RequestParam int operateStatus,
//            @RequestHeader("currentUserId") String currentUserId) { // 新的操作状态
//        try {
//            // 调用服务层方法更新状态
//            EmailTask updatedTask = emailTaskService.updateOperateStatus(emailTaskId, operateStatus, currentUserId);
//            // 返回成功结果
//            return Result.success(updatedTask);
//        } catch (IllegalArgumentException e) {
//            // 返回参数错误信息
//            return Result.error(e.getMessage());
//        } catch (Exception e) {
//            // 返回系统错误信息
//            return Result.error("更新任务操作状态失败");
//        }
//    }
//
//    /**
//     * 更新生日邮件任务的状态、主题、模板ID和附件
//     *
//     * @param emailTaskId 邮件任务ID
//     * @param request     请求体，包含 operateStatus、subject、templateId 和 attachments
//     * @return 更新结果
//     */
//    @PutMapping("/{emailTaskId}")
//    public Result updateEmailTask(
//            @PathVariable String emailTaskId,
//            @RequestBody UpdateBirthdayEmailTaskRequest request) {
//        try {
//            // 调用服务层方法更新邮件任务
//            EmailTask updatedTask = emailTaskService.updateBirthdayEmailTask(
//                    emailTaskId,
//                    request.getOperateStatus(),
//                    request.getSubject(),
//                    request.getTemplateId(),
//                    request.getAttachments()
//            );
//            // 返回成功结果
//            return Result.success(updatedTask);
//        } catch (IllegalArgumentException e) {
//            // 返回参数错误信息
//            return Result.error(e.getMessage());
//        } catch (Exception e) {
//            // 返回系统错误信息
//            return Result.error("更新邮件任务失败");
//        }
//    }
//
//    /**
//     * 查询节日发送的邮件任务
//     *
//     * @return 节日发送的邮件任务列表
//     */
//    @GetMapping("/festival")
//    public List<EmailTask> getFestivalTasks() {
//        return emailTaskService.findFestivalTasks();
//    }
//
//    /**
//     * 查询生日发送的邮件任务
//     *
//     * @return 生日发送的邮件任务列表
//     */
//    @GetMapping("/birthday")
//    public List<EmailTask> getBirthdayTasks() {
//        return emailTaskService.findBirthdayTasks();
//    }
//
//    /**
//     * 创建手动发送邮件的任务
//     *
//     * @return 该邮件任务对象
//     */
//    @PostMapping("/create")
//    public Result<EmailTask> createEmailTask(@RequestBody CreateEmailTaskRequest request) {
//        return Result.success(emailTaskService.createEmailTask(request));
//    }
//
//    /**
//     * 创建循环发送邮件的任务
//     *
//     * @return 该邮件任务对象
//     */
//    @PostMapping("/createCycle")
//    public Result<EmailTask> createEmailTask(@RequestBody CreateCycleEmailTaskRequest request) {
//        return Result.success(emailTaskService.createCycleEmailTask(request));
//    }
//
//    /**
//     * 创建节日发送邮件的任务
//     *
//     * @return 该邮件任务对象
//     */
//    @PostMapping("/createFestival")
//    public Result<EmailTask> createFestivalEmailTask(@RequestBody CreateFestivalEmailTaskRequest request) {
//        return Result.success(emailTaskService.createFestivalEmailTask(request));
//    }
//}
//
