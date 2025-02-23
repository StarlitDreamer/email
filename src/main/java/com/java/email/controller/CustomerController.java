package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Customer;
import com.java.email.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    /**
     * 根据条件筛选客户
     *
     * @param belongUserId  所属用户ID
     * @param customerLevel 客户等级
     * @param customerName  客户名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param pageNumber          页码
     * @param size          每页大小
     * @return 符合条件的客户分页结果
     */
    @GetMapping("/search")
    public Result<Page<Customer>> findCustomersByCriteria(
            @RequestParam(required = false) String belongUserId,
            @RequestParam(required = false) Integer customerLevel,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("currentUserId") String currentUserId, // 从请求头中获取当前用户ID
            @RequestHeader("currentUserRole") int currentUserRole) { // 从请求头中获取当前用户角色

        // 调用服务层方法，传递当前用户ID和角色
        return customerService.findCustomersByCriteria(
                belongUserId, customerLevel, customerName, status, tradeType,
                pageNumber-1, size, currentUserId, currentUserRole);
    }
//    @GetMapping("/search")
//    public Result<Page<Customer>> findCustomersByCriteria(
//            @RequestParam(required = false) String belongUserId,
//            @RequestParam(required = false) Integer customerLevel,
//            @RequestParam(required = false) String customerName,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return customerService.findCustomersByCriteria(
//                belongUserId, customerLevel, customerName, status, tradeType, page, size);
//    }

    /**
     * 根据条件筛选客户并存入redis中，返回redisKey
     *
     * @param belongUserId  所属用户ID
     * @param customerLevel 客户等级
     * @param customerName  客户名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param pageNumber          页码
     * @param size          每页大小
     * @return 符合条件的客户分页结果
     */
    @GetMapping("/search-all")
    public Result<String> findCustomersByCriteriaRedis(
            @RequestParam(required = false) String belongUserId,
            @RequestParam(required = false) Integer customerLevel,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("currentUserId") String currentUserId, // 从请求头中获取当前用户ID
            @RequestHeader("currentUserRole") int currentUserRole) { // 从请求头中获取当前用户角色

        // 调用服务层方法，传递当前用户ID和角色
        return customerService.findCustomersByCriteriaRedis(
                belongUserId, customerLevel, customerName, status, tradeType,
                pageNumber-1, size, currentUserId, currentUserRole);
    }
//    @GetMapping("/search-redis")
//    public Result<String> findCustomersByCriteriaRedis(
//            @RequestParam(required = false) String belongUserId,
//            @RequestParam(required = false) Integer customerLevel,
//            @RequestParam(required = false) String customerName,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return customerService.findCustomersByCriteriaRedis(
//                belongUserId, customerLevel, customerName, status, tradeType, page, size);
//    }
}
