package com.java.email.controller;

import com.java.email.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    /**
     * 根据 customerId 数组查询对应的 emails，并去除重复的 emails。
     *
     * @param customerIds 客户 ID 数组
     * @return 去重后的 emails 列表
     */
    @PostMapping("/emails")
    public List<String> getUniqueEmails(@RequestBody List<String> customerIds) {
        return customerService.getUniqueEmailsByCustomerIds(customerIds);
    }
}