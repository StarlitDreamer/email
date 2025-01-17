package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Customer;
import com.java.email.entity.Supplier;
import com.java.email.repository.CustomerRepository;
import com.java.email.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class RecipientService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * 根据条件筛选客户或供应商
     *
     * @param type          类型（customer 或 supplier）
     * @param ownerUserId   所属用户ID
     * @param level         等级（客户等级或供应商等级）
     * @param name          名称（客户名称或供应商名称）
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的客户或供应商分页结果
     */
    public Result<Page<?>> findRecipientsByCriteria(String type, String ownerUserId, Integer level,
                                                    String name, Integer status, Integer tradeType,
                                                    int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            if ("customer".equalsIgnoreCase(type)) {
                // 查询客户
                Page<Customer> customers;

                if (ownerUserId != null) {
                    customers = customerRepository.findByOwnerUserId(ownerUserId, pageable);
                } else if (level != null) {
                    customers = customerRepository.findByCustomerLevel(level, pageable);
                } else if (name != null) {
                    customers = customerRepository.findByCustomerName(name, pageable);
                } else if (status != null) {
                    customers = customerRepository.findByStatus(status, pageable);
                } else if (tradeType != null) {
                    customers = customerRepository.findByTradeType(tradeType, pageable);
                } else {
                    customers = customerRepository.findAll(pageable);
                }

                return Result.success(customers);
            } else if ("supplier".equalsIgnoreCase(type)) {
                // 查询供应商
                Page<Supplier> suppliers;

                if (ownerUserId != null) {
                    suppliers = supplierRepository.findByOwnerUserId(ownerUserId, pageable);
                } else if (level != null) {
                    suppliers = supplierRepository.findBySupplierLevel(level, pageable);
                } else if (name != null) {
                    suppliers = supplierRepository.findBySupplierName(name, pageable);
                } else if (status != null) {
                    suppliers = supplierRepository.findByStatus(status, pageable);
                } else if (tradeType != null) {
                    suppliers = supplierRepository.findByTradeType(tradeType, pageable);
                } else {
                    suppliers = supplierRepository.findAll(pageable);
                }

                return Result.success(suppliers);
            } else {
                return Result.error("类型参数无效，请指定 'customer' 或 'supplier'");
            }
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
//    /**
//     * 根据条件筛选客户
//     *
//     * @param ownerUserId   所属用户ID
//     * @param customerLevel 客户等级
//     * @param customerName  客户名称
//     * @param status        分配状态
//     * @param tradeType     贸易类型
//     * @param page          页码
//     * @param size          每页大小
//     * @return 符合条件的客户分页结果
//     */
//    public Result<Page<Customer>> findCustomersByCriteria(String ownerUserId, Integer customerLevel,
//                                                          String customerName, Integer status, Integer tradeType,
//                                                          int page, int size) {
//        try {
//            Page<Customer> customers;
//
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (ownerUserId != null) {
//                customers = customerRepository.findByOwnerUserId(ownerUserId, pageable);
//            } else if (customerLevel != null) {
//                customers = customerRepository.findByCustomerLevel(customerLevel, pageable);
//            } else if (customerName != null) {
//                customers = customerRepository.findByCustomerName(customerName, pageable);
//            } else if (status != null) {
//                customers = customerRepository.findByStatus(status, pageable);
//            } else if (tradeType != null) {
//                customers = customerRepository.findByTradeType(tradeType, pageable);
//            } else {
//                // 如果没有条件，返回所有客户（分页）
//                customers = customerRepository.findAll(pageable);
//            }
//
//            // 返回成功结果
//            return Result.success(customers);
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询客户失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 根据条件筛选供应商
//     *
//     * @param ownerUserId   所属用户ID
//     * @param supplierLevel 供应商等级
//     * @param supplierName  供应商名称
//     * @param status        分配状态
//     * @param tradeType     贸易类型
//     * @param page          页码
//     * @param size          每页大小
//     * @return 符合条件的供应商分页结果
//     */
//    public Result<Page<Supplier>> findSuppliersByCriteria(String ownerUserId, Integer supplierLevel,
//                                                          String supplierName, Integer status, Integer tradeType,
//                                                          int page, int size) {
//        try {
//            Page<Supplier> suppliers;
//
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (ownerUserId != null) {
//                suppliers = supplierRepository.findByOwnerUserId(ownerUserId, pageable);
//            } else if (supplierLevel != null) {
//                suppliers = supplierRepository.findBySupplierLevel(supplierLevel, pageable);
//            } else if (supplierName != null) {
//                suppliers = supplierRepository.findBySupplierName(supplierName, pageable);
//            } else if (status != null) {
//                suppliers = supplierRepository.findByStatus(status, pageable);
//            } else if (tradeType != null) {
//                suppliers = supplierRepository.findByTradeType(tradeType, pageable);
//            } else {
//                // 如果没有条件，返回所有供应商（分页）
//                suppliers = supplierRepository.findAll(pageable);
//            }
//
//            // 返回成功结果
//            return Result.success(suppliers);
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询供应商失败: " + e.getMessage());
//        }
//    }
}
