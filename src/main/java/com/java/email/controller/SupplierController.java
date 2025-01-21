package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Supplier;
import com.java.email.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

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
    @GetMapping("/search")
    public Result<Page<Supplier>> findSuppliersByCriteria(
            @RequestParam(required = false) String ownerUserId,
            @RequestParam(required = false) Integer supplierLevel,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return supplierService.findSuppliersByCriteria(
                ownerUserId, supplierLevel, supplierName, status, tradeType, page, size);
    }
}
