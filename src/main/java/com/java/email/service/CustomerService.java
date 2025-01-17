package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Customer;
import com.java.email.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

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
    public Result<Page<Customer>> findCustomersByCriteria(String ownerUserId, Integer customerLevel,
                                                          String customerName, Integer status, Integer tradeType,
                                                          int page, int size) {
        try {
            Page<Customer> customers;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 动态构建查询条件
            if (ownerUserId != null) {
                customers = customerRepository.findByOwnerUserId(ownerUserId, pageable);
            } else if (customerLevel != null) {
                customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
            } else if (customerName != null) {
                customers = customerRepository.findByCustomerName(customerName, pageable);
            } else if (status != null) {
                customers = customerRepository.findByStatus(status, pageable);
            } else if (tradeType != null) {
                customers = customerRepository.findByTradeType(tradeType, pageable);
            } else {
                // 如果没有条件，返回所有客户（分页）
                customers = customerRepository.findAll(pageable);
            }

            // 返回成功结果
            return Result.success(customers);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询客户失败: " + e.getMessage());
        }
    }
}