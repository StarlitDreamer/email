package com.java.email.controller;

import com.java.email.model.dto.FilterSupplierDto;
import com.java.email.model.dto.SearchAllSupplierDto;
import com.java.email.model.response.FilterSupplierResponse;
import com.java.email.model.response.SearchAllCustomerResponse;
import com.java.email.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

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
            @RequestBody FilterSupplierDto filterSuppliersDto) throws IOException {

        // 调用服务层方法
        FilterSupplierResponse response = supplierService.FilterFindSupplier(currentUserId, currentUserRole, filterSuppliersDto);

        // 返回成功响应
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/filterAll")
    public ResponseEntity<SearchAllCustomerResponse> filterFindAllSupplier(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody SearchAllSupplierDto searchAllSupplierDto) {

        try {
            // 调用服务层方法获取供应商数据
            SearchAllCustomerResponse response = supplierService.FindFindAllSupplier(currentUserId, currentUserRole, searchAllSupplierDto);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            // 如果发生异常，返回500错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchAllCustomerResponse(0, "Error fetching supplier data"));
        }
    }
}
