package com.java.email.repository;

import com.java.email.model.entity.Supplier;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SupplierRepository extends ElasticsearchRepository<Supplier, String> {
    // 根据 supplierId 列表查询对应的供应商邮箱
    List<Supplier> findBySupplierIdIn(List<String> supplierIds);

    Supplier findByEmails(String email);  // 按照邮箱查找供应商

    //
    Supplier findBySupplierId(String supplierId);
}