package com.java.email.service;

import com.java.email.pojo.Customer;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface CustomerService {
    /**
     * 根据客户等级查找所有邮箱
     */
    List<String> findEmailsByCustomerLevel(long customerLevel) throws IOException;

    /**
     * 根据邮箱查找客户对象
     */
    Customer findCustomerByEmail(String email) throws IOException;


    List<String> findMatchingCustomerEmails(Map<String, String> params) throws IOException;

    /**
     * 批量查询邮箱对应的客户对象
     */
    Map<String, Customer> findCustomersByEmails(List<String> emails) throws IOException;

    void saveCustomer(Customer customer) throws IOException;
} 