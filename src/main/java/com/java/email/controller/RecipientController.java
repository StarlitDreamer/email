package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Customer;
import com.java.email.entity.Supplier;
import com.java.email.service.CustomerService;
import com.java.email.service.RecipientService;
import com.java.email.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipients")
public class RecipientController {

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SupplierService supplierService;

    /**
     * 根据条件筛选客户或供应商，并将结果存储到 Redis 中
     *
     * @param type        类型（customer 或 supplier）
     * @param ownerUserId 所属用户ID
     * @param level       等级（客户等级或供应商等级）
     * @param name        名称（客户名称或供应商名称）
     * @param status      分配状态
     * @param tradeType   贸易类型
     * @param page        页码
     * @param size        每页大小
     * @return Redis Key，用于获取存储的数据
     */
    @GetMapping("/search-and-store")
    public Result<String> findRecipientsAndStoreInRedis(
            @RequestParam(required = false, defaultValue = "customer") String type,
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return recipientService.findRecipientsAndStoreInRedis(
                type, ownerUserId, level, name, status, tradeType, page, size);
    }

    /**
     * 根据条件筛选客户和供应商作为收件人，并将结果存储到 Redis
     *
     * @param acceptedEmailTypeId 接受的邮件类型 ID 列表
     * @param tradeType           贸易类型（1: 工厂, 2: 贸易商）
     * @param customerLevel       客户等级（1: 初级, 2: 中级, 3: 高级）
     * @param supplierLevel       供应商等级（1: 初级, 2: 中级, 3: 高级）
     * @param status              分配状态（1: 未分配, 2: 已分配）
     * @return Redis 的 key
     */
//    @GetMapping("/filter-and-store")
//    public Result<String> filterAndStoreRecipients(
//            @RequestParam(required = false) List<String> acceptedEmailTypeId,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(required = false) Integer customerLevel,
//            @RequestParam(required = false) Integer supplierLevel,
//            @RequestParam(required = false) Integer status) {
//        try {
//            String redisKey = recipientService.filterAndStoreRecipients(acceptedEmailTypeId, tradeType, customerLevel, supplierLevel, status);
//            return Result.success(redisKey);
//        } catch (Exception e) {
//            return Result.error("筛选并存储收件人失败: " + e.getMessage());
//        }
//    }

    /**
     * 根据条件筛选客户或供应商
     *
     * @param type        类型（customer 或 supplier）
     * @param ownerUserId 所属用户ID
     * @param level       等级（客户等级或供应商等级）
     * @param name        名称（客户名称或供应商名称）
     * @param status      分配状态
     * @param tradeType   贸易类型
     * @param page        页码
     * @param size        每页大小
     * @return 符合条件的客户或供应商分页结果
     */
    @GetMapping("/search")
    public Result<Page<?>> findRecipientsByCriteria(
            @RequestParam(required = false, defaultValue = "customer") String type,
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("currentUserId") String currentUserId, // 从请求头中获取当前用户ID
            @RequestHeader("currentUserRole") int currentUserRole) { // 从请求头中获取当前用户角色

        return recipientService.findRecipientsByCriteria(
                currentUserId, currentUserRole, type, ownerUserId, level, name, status, tradeType, page, size);
    }
//    @GetMapping("/search")
//    public Result<Page<?>> findRecipientsByCriteria(
//            @RequestParam(required = false, defaultValue = "customer") String type,
//            @RequestParam(required = false) String ownerUserId,
//            @RequestParam(required = false) Integer level,
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return recipientService.findRecipientsByCriteria(
//                type, ownerUserId, level, name, status, tradeType, page, size);
//    }

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
//    @GetMapping("/customers/search")
//    public Result<Page<Customer>> findCustomersByCriteria(
//            @RequestParam(required = false) String ownerUserId,
//            @RequestParam(required = false) Integer customerLevel,
//            @RequestParam(required = false) String customerName,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return customerService.findCustomersByCriteria(
//                ownerUserId, customerLevel, customerName, status, tradeType, page, size);
//    }

    /**
     * 根据条件筛选供应商
     *
     * @param ownerUserId   所属用户ID
     * @param supplierLevel 供应商等级
     * @param supplierName  供应商名称
     * @param status        分配状态
     * @param tradeType     贸易类型
     * @param page          页码
     * @param size          每页大小
     * @return 符合条件的供应商分页结果
     */
//    @GetMapping("/suppliers/search")
//    public Result<Page<Supplier>> findSuppliersByCriteria(
//            @RequestParam(required = false) String ownerUserId,
//            @RequestParam(required = false) Integer supplierLevel,
//            @RequestParam(required = false) String supplierName,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) Integer tradeType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return supplierService.findSuppliersByCriteria(
//                ownerUserId, supplierLevel, supplierName, status, tradeType, page, size);
//    }

    // 以下为保留的注释代码，方便后续扩展
    /*
    @GetMapping("/customers/search")
    public Result<Page<Customer>> findCustomersByCriteria(
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer customerLevel,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 实现逻辑
    }

    @GetMapping("/suppliers/search")
    public Result<Page<Supplier>> findSuppliersByCriteria(
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer supplierLevel,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 实现逻辑
    }
    */
}