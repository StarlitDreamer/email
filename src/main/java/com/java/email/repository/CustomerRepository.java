package com.java.email.repository;

import com.java.email.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomerRepository extends ElasticsearchRepository<Customer, String> {

    // 根据客户 ID 查找客户
    Customer findByCustomerId(String customerId);

    // 自定义查询方法（分页）
    Page<Customer> findByOwnerUserId(String ownerUserId, Pageable pageable);

    Page<Customer> findByCustomerLevel(int customerLevel, Pageable pageable);

    Page<Customer> findByCustomerName(String customerName, Pageable pageable);

    Page<Customer> findByStatus(int status, Pageable pageable);

    Page<Customer> findByTradeType(int tradeType, Pageable pageable);

    // 分页查询方法
    Page<Customer> findAll(Pageable pageable);
}