package com.java.email.repository;

import com.java.email.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SupplierRepository extends ElasticsearchRepository<Supplier, String> {

    // 根据供应商 ID 查找供应商
    Supplier findBySupplierId(String supplierId);

    // 根据所属用户 ID 查询供应商（分页）
    Page<Supplier> findByBelongUserid(String belongUserid, Pageable pageable);

    // 根据供应商等级查询供应商（分页）
    Page<Supplier> findBySupplierLevel(int supplierLevel, Pageable pageable);

    // 根据供应商名称查询供应商（分页）
    Page<Supplier> findBySupplierName(String supplierName, Pageable pageable);

    // 根据分配状态查询供应商（分页）
    Page<Supplier> findByStatus(int status, Pageable pageable);

    // 根据贸易类型查询供应商（分页）
    Page<Supplier> findByTradeType(int tradeType, Pageable pageable);

    // 分页查询所有供应商
    Page<Supplier> findAll(Pageable pageable);



    Page<Supplier> findByBelongUseridIn(List<String> allowedUserIds, Pageable pageable);

    /**
     * 根据条件筛选供应商
     *
     * @param acceptedEmailTypeId 接受的邮件类型 ID 列表
     * @param tradeType           贸易类型
     * @param supplierLevel       供应商等级
     * @param status              分配状态
     * @return 符合条件的供应商列表
     */
//    List<Supplier> findByCriteria(List<String> acceptedEmailTypeId, Integer tradeType, Integer supplierLevel, Integer status);
}