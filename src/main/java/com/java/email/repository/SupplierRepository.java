package com.java.email.repository;

import com.java.email.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface SupplierRepository extends ElasticsearchRepository<Supplier, String> {

    // 根据供应商 ID 查找供应商
    Supplier findBySupplierId(String supplierId);

    // 自定义查询方法（分页）
    Page<Supplier> findByOwnerUserId(String ownerUserId, Pageable pageable);

    Page<Supplier> findBySupplierLevel(int supplierLevel, Pageable pageable);

    Page<Supplier> findBySupplierName(String supplierName, Pageable pageable);

    Page<Supplier> findByStatus(int status, Pageable pageable);

    Page<Supplier> findByTradeType(int tradeType, Pageable pageable);

    // 分页查询方法
    Page<Supplier> findAll(Pageable pageable);
}
