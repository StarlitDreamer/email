package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.entity.Customer;
import com.java.email.model.entity.EmailReport;
import com.java.email.model.entity.Supplier;
import com.java.email.repository.CustomerRepository;
import com.java.email.repository.SupplierRepository;
import com.java.email.service.EmailReportService;
import com.java.email.service.EmailTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/email-report")
public class EmailReportController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private EmailReportService emailReportService;

    @Autowired
    private EmailTaskService emailTaskService;

    /**
     * 用户点击退订链接，根据email_task_id增加退订数量。
     *
     * @param emailTaskId 邮件任务ID
     * @return 返回更新结果
     */
    @GetMapping("/unsubscribe")
    public Result updateUnsubscribeAmount(@RequestParam String emailTaskId, @RequestParam String receiverEmail) {
        String emailTypeId = emailTaskService.getEmailTypeId(emailTaskId);

        Customer customer = customerRepository.findByEmails(receiverEmail);

        if (customer != null) {

            // 将emailTypeId添加到noAcceptEmailTypeId
            if (customer.getNoAcceptEmailTypeId() == null) {
                customer.setNoAcceptEmailTypeId(new ArrayList<>()); // 如果noAcceptEmailTypeId为空，初始化为空列表
            }

            customer.getNoAcceptEmailTypeId().add(emailTypeId);

            customerRepository.save(customer);  // 保存更新后的Customer实体
        }

        Supplier supplier = supplierRepository.findByEmails(receiverEmail);

        if (supplier != null) {
            // 将emailTypeId添加到noAcceptEmailTypeId
            if (supplier.getNoAcceptEmailTypeId() == null) {
                supplier.setNoAcceptEmailTypeId(new ArrayList<>()); // 如果noAcceptEmailTypeId为空，初始化为空列表
            }

            supplier.getNoAcceptEmailTypeId().add(emailTypeId);

            supplierRepository.save(supplier);  // 保存更新后的supplier实体
        }

        try {
            EmailReport updatedEmailReport = emailReportService.updateUnsubscribeAmount(emailTaskId);
            return Result.success(updatedEmailReport);
        } catch (Exception e) {
            return Result.error("更新退订数量失败: " + e.getMessage());
        }
    }

    /**
     * 根据email_task_id更新打开数量
     *
     * @param emailTaskId 邮件任务ID
     * @return 更新后的EmailReport实体
     */
    @PutMapping("/open-email/{emailTaskId}")
    public Result updateOpenAmount(@PathVariable String emailTaskId) {
        try {
            EmailReport updatedEmailReport = emailReportService.updateOpenAmount(emailTaskId);
            return Result.success(updatedEmailReport);
        } catch (Exception e) {
            return Result.error("更新打开数量失败: " + e.getMessage());
        }
    }
}