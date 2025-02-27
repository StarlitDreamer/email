package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.dto.FilterSupplierDto;
import com.java.email.model.dto.SearchAllSupplierDto;
import com.java.email.model.response.FilterAllReceiverResponse;
import com.java.email.model.response.FilterReceiverResponse;
import com.java.email.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 过滤查找供应商
     *
     * @param currentUserId      当前用户ID
     * @param currentUserRole    当前用户角色
     * @param filterSuppliersDto 过滤条件
     * @return 过滤后的供应商列表
     * @throws IOException 如果与 Elasticsearch 交互时出现问题
     */
    @PostMapping("/filter")
    public Result filterFindSupplier(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody FilterSupplierDto filterSuppliersDto) {
        try {
            // 调用服务层方法
            FilterReceiverResponse response = supplierService.FilterFindSupplier(currentUserId, currentUserRole, filterSuppliersDto);
            // 返回成功响应
            return Result.success(response);
        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/filterAll")
    public Result filterFindAllSupplier(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody SearchAllSupplierDto searchAllSupplierDto) {
        try {
            // 调用服务层方法
            FilterAllReceiverResponse response = supplierService.FindFindAllSupplier(currentUserId, currentUserRole, searchAllSupplierDto);
            // 返回成功响应
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
