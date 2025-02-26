//package com.java.email.service;
//
//import com.java.email.entity.EmailDetail;
//import com.java.email.entity.EmailReport;
//import com.java.email.repository.EmailDetailRepository;
//import com.java.email.repository.EmailReportRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class EmailDetailService {
//
//    @Autowired
//    private EmailDetailRepository emailDetailRepository;
//
//
//    @Autowired
//    private EmailReportRepository emailReportRepository;
//
//    // 获取所有状态码为 500 的邮件的 emailTaskId
//    public List<String> getEmailTaskIdsForErrorCode500() {
//        List<EmailDetail> emailDetails = emailDetailRepository.findByErrorCode(500);
//        return emailDetails.stream()
//                .map(EmailDetail::getEmailTaskId)
//                .collect(Collectors.toList());
//    }
//
//    // 获取退信数量
//    public long getBounceCount() {
//        return emailDetailRepository.countByErrorCode(500);
//    }
//
//    // 获取送达数量
//    public long getDeliveredCount() {
//        return emailDetailRepository.countByErrorCode(200);
//    }
//
////    // 每隔一小时执行一次统计，并将结果保存到 email_report 索引
////    @Scheduled(cron = "0 * * * * ?")  // 每小时执行一次
////    public void generateEmailReport() {
////        long bounceCount = getBounceCount();
////        long deliveredCount = getDeliveredCount();
//////        long currentTime = System.currentTimeMillis() / 1000;  // 获取当前时间（秒级时间戳）
////
////        // 创建报告
////        EmailReport emailReport = new EmailReport();
//////        emailReport.setReportTime(currentTime);
////        emailReport.setBounceAmount(bounceCount);
////        emailReport.setDeliveryAmount(deliveredCount);
////
////        // 保存到 email_report 索引
////        emailReportRepository.save(emailReport);
////
////        // 打印日志，确认任务执行
////        System.out.println("Email report generated. Bounce Count: " + bounceCount + ", Delivered Count: " + deliveredCount);
////    }
//
//    /**
//     * 统计指定 emailTaskId 下 errorCode 为 200 的 emailId 数量
//     *
//     * @param emailTaskId 邮件任务ID
//     * @return 成功数量
//     */
//    public long countSuccessEmailIds(String emailTaskId) {
//        return countEmailIdsByErrorCode(emailTaskId, 200);
//    }
//
//    /**
//     * 统计指定 emailTaskId 下 errorCode 为 500 的 emailId 数量
//     *
//     * @param emailTaskId 邮件任务ID
//     * @return 失败数量
//     */
//    public long countFailedEmailIds(String emailTaskId) {
//        return countEmailIdsByErrorCode(emailTaskId, 500);
//    }
//
//    /**
//     * 遍历整个 email_details 索引，统计每个 email_task_id 的 error_code 为 200 和 500 的数量
//     * 并将结果存储到 email_report 索引中
//     */
//    @Scheduled(cron = "0 * * * * ?")  // 每执行一次
//    public void generateEmailReport() {
//        // 获取所有 email_task_id 的唯一列表
//        Iterable<EmailDetail> emailDetailsIterable = emailDetailRepository.findAll();
//        List<EmailDetail> allEmailDetails = new ArrayList<>();
//        emailDetailsIterable.forEach(allEmailDetails::add);
//
//        // 按 email_task_id 分组
//        Map<String, Set<String>> emailTaskIdMap = new HashMap<>();
//        for (EmailDetail emailDetail : allEmailDetails) {
//            String emailTaskId = emailDetail.getEmailTaskId();
//            emailTaskIdMap.computeIfAbsent(emailTaskId, k -> new HashSet<>()).add(emailDetail.getEmailId());
//        }
//
//        // 遍历每个 email_task_id，统计 error_code 为 200 和 500 的数量
//        for (Map.Entry<String, Set<String>> entry : emailTaskIdMap.entrySet()) {
//            String emailTaskId = entry.getKey();
//            Set<String> emailIds = entry.getValue();
//
//            // 统计 error_code 为 200 的数量
//            long successCount = countEmailIdsByErrorCode(emailTaskId, 200);
//
//            // 统计 error_code 为 500 的数量
//            long failedCount = countEmailIdsByErrorCode(emailTaskId, 500);
//
//            // 创建 EmailReport 对象
//            EmailReport emailReport = new EmailReport();
//            emailReport.setEmailTaskId(emailTaskId);
//            emailReport.setDeliveryAmount(successCount);  // 送达数量
//            emailReport.setBounceAmount(failedCount);     // 退信数量
//            emailReport.setEmailTotal((long) emailIds.size());   // 邮件总数
//
//            // 打印日志，确认任务执行
//            System.out.println("Email report generated. successCount : " + successCount + ", failedCount : " + failedCount);
//
//            // 保存到 email_report 索引
//            emailReportRepository.save(emailReport);
//        }
//    }
////    public void generateEmailReport() {
////        // 获取所有 email_task_id 的唯一列表
////        List<EmailDetail> allEmailDetails = emailDetailRepository.findAll();
////        Map<String, Set<String>> emailTaskIdMap = new HashMap<>();
////
////        // 按 email_task_id 分组
////        for (EmailDetail emailDetail : allEmailDetails) {
////            String emailTaskId = emailDetail.getEmailTaskId();
////            emailTaskIdMap.computeIfAbsent(emailTaskId, k -> new HashSet<>()).add(emailDetail.getEmailId());
////        }
////
////        // 遍历每个 email_task_id，统计 error_code 为 200 和 500 的数量
////        for (Map.Entry<String, Set<String>> entry : emailTaskIdMap.entrySet()) {
////            String emailTaskId = entry.getKey();
////            Set<String> emailIds = entry.getValue();
////
////            // 统计 error_code 为 200 的数量
////            long successCount = countEmailIdsByErrorCode(emailTaskId, 200);
////
////            // 统计 error_code 为 500 的数量
////            long failedCount = countEmailIdsByErrorCode(emailTaskId, 500);
////
////            // 创建 EmailReport 对象
////            EmailReport emailReport = new EmailReport();
////            emailReport.setEmailTaskId(emailTaskId);
////            emailReport.setDeliveryAmount(successCount);  // 送达数量
////            emailReport.setBounceAmount(failedCount);     // 退信数量
////            emailReport.setEmailTotal((long) emailIds.size());   // 邮件总数
////
////            // 打印日志，确认任务执行
////            System.out.println("Email report generated. successCount : " + successCount + ", failedCount : " + failedCount);
////
////
////            // 保存到 email_report 索引
////            emailReportRepository.save(emailReport);
////        }
////    }
//
//    /**
//     * 根据 emailTaskId 和 errorCode 统计 emailId 数量
//     *
//     * @param emailTaskId 邮件任务ID
//     * @param errorCode   错误码
//     * @return 数量
//     */
////    private long countEmailIdsByErrorCode(String emailTaskId, Integer errorCode) {
////        // 查询符合条件的 EmailDetail 列表
////        List<EmailDetail> emailDetails = emailDetailRepository.findByEmailTaskIdAndErrorCode(emailTaskId, errorCode);
////
////        // 使用 Set 去重 emailId
////        Set<String> emailIdSet = new HashSet<>();
////        for (EmailDetail emailDetail : emailDetails) {
////            emailIdSet.add(emailDetail.getEmailId());
////        }
////
////        // 返回去重后的数量
////        return emailIdSet.size();
////    }
//}
