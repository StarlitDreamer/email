package com.java.email.repository;

import com.java.email.model.entity.Customer;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CustomerRepository extends ElasticsearchRepository<Customer, String> {
    // 根据 customerId 列表查询对应的客户邮箱
    List<Customer> findByCustomerIdIn(List<String> customerIds);

    Customer findByEmails(String email);  // 按照邮箱查找客户

    // 根据客户ID查找客户
    Customer findByCustomerId(String customerId);
}