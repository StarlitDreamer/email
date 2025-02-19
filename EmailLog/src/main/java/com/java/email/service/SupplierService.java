package com.java.email.service;

import com.java.email.pojo.Supplier;

import java.util.List;

public interface SupplierService {
    
    /**
     * 保存供应商信息
     */
    Supplier saveSupplier(Supplier supplier);
    
    /**
     * 根据供应商ID查询
     */
    Supplier findById(String supplierId);
    
    /**
     * 根据邮箱查询供应商
     */
    List<Supplier> findByEmail(String email);
    
   
} 