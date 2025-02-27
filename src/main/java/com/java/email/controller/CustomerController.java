package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.dto.FilterCustomerDto;
import com.java.email.model.dto.SearchAllCustomerDto;
import com.java.email.model.response.FilterAllReceiverResponse;
import com.java.email.model.response.FilterReceiverResponse;
import com.java.email.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 过滤查找客户
     *
     * @param currentUserId      当前用户ID
     * @param currentUserRole    当前用户角色
     * @param filterCustomersDto 过滤条件
     * @return 过滤后的客户列表
     */
    @PostMapping("/filter")
    public Result filterFindCustomer(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody FilterCustomerDto filterCustomersDto) {
        try {
            // 调用服务层方法
            FilterReceiverResponse response = customerService.FilterFindCustomers(currentUserId, currentUserRole, filterCustomersDto);
            // 返回成功响应
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/filterAll")
    public Result filterFindAllCustomer(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody SearchAllCustomerDto searchAllCustomerDto) {
        try {
            // 调用服务层方法
            FilterAllReceiverResponse response = customerService.findFindAllCustomer(currentUserId, currentUserRole, searchAllCustomerDto);
            // 返回成功响应
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
