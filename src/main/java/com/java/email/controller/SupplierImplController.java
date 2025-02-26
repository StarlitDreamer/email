package com.java.email.controller;

import com.java.email.dto.FilterSupplierResponse;
import com.java.email.dto.FilterSuppliersDto;
import com.java.email.service.SupplierServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/supplier")
public class SupplierImplController {

    @Autowired
    private SupplierServiceImpl supplierService;

    /**
     * 过滤查找供应商
     * @param currentUserId 当前用户ID
     * @param currentUserRole 当前用户角色
     * @param filterSuppliersDto 过滤条件
     * @return 过滤后的供应商列表
     * @throws IOException 如果与 Elasticsearch 交互时出现问题
     */
    @PostMapping("/filter")
    public ResponseEntity<FilterSupplierResponse> filterFindSupplier(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody FilterSuppliersDto filterSuppliersDto) throws IOException {

        // 调用服务层方法
        FilterSupplierResponse response = supplierService.FilterFindSupplier(currentUserId, currentUserRole, filterSuppliersDto);

        // 返回成功响应
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
