package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Customer;
import com.java.email.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    /**
     * 根据条件筛选客户
     *
     * @param ownerUserId   所属用户ID
     * @param customerLevel 客户等级
     * @param customerName  客户名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的客户分页结果
     */
    @GetMapping("/customers/search")
    public Result<Page<Customer>> findCustomersByCriteria(
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer customerLevel,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return customerService.findCustomersByCriteria(
                ownerUserId, customerLevel, customerName, status, tradeType, page, size);
    }
}
