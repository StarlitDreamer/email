package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Supplier;
import com.java.email.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

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
    public Result<Page<Supplier>> findSuppliersByCriteria(String ownerUserId, Integer supplierLevel,
                                                          String supplierName, Integer status, Integer tradeType,
                                                          int page, int size) {
        try {
            Page<Supplier> suppliers;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 动态构建查询条件
            if (ownerUserId != null) {
                suppliers = supplierRepository.findByOwnerUserId(ownerUserId, pageable);
            } else if (supplierLevel != null) {
                suppliers = supplierRepository.findBySupplierLevel(supplierLevel, pageable);
            } else if (supplierName != null) {
                suppliers = supplierRepository.findBySupplierName(supplierName, pageable);
            } else if (status != null) {
                suppliers = supplierRepository.findByStatus(status, pageable);
            } else if (tradeType != null) {
                suppliers = supplierRepository.findByTradeType(tradeType, pageable);
            } else {
                // 如果没有条件，返回所有供应商（分页）
                suppliers = supplierRepository.findAll(pageable);
            }

            // 返回成功结果
            return Result.success(suppliers);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询供应商失败: " + e.getMessage());
        }
    }
}
