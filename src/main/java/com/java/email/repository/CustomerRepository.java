package com.java.email.repository;

import com.java.email.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CustomerRepository extends ElasticsearchRepository<Customer, String> {

    // 根据客户 ID 查找客户
    Customer findByCustomerId(String customerId);

    // 根据所属用户 ID 查询客户（分页）
    Page<Customer> findByBelongUserId(String belongUserId, Pageable pageable);

    // 根据客户等级查询客户（分页）
    Page<Customer> findByCustomerLevel(int customerLevel, Pageable pageable);

    // 根据客户名称查询客户（分页）
    Page<Customer> findByCustomerName(String customerName, Pageable pageable);

    // 根据分配状态查询客户（分页）
    Page<Customer> findByStatus(int status, Pageable pageable);

    // 根据贸易类型查询客户（分页）
    Page<Customer> findByTradeType(int tradeType, Pageable pageable);

    // 分页查询所有客户
    Page<Customer> findAll(Pageable pageable);



    Page<Customer> findByBelongUserIdIn(List<String> allowedUserIds, Pageable pageable);

    /**
     * 根据条件筛选客户
     *
     * @param acceptedEmailTypeId 接受的邮件类型 ID 列表
     * @param tradeType           贸易类型
     * @param customerLevel       客户等级
     * @param status              分配状态
     * @return 符合条件的客户列表
     */
//    List<Customer> findByCriteria(List<String> acceptedEmailTypeId, Integer tradeType, Integer customerLevel, Integer status);
}