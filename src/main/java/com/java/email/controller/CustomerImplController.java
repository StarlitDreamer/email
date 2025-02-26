package com.java.email.controller;

import com.java.email.model.dto.FilterCustomerDto;
import com.java.email.model.dto.SearchAllCustomerDto;
import com.java.email.model.response.FilterCustomerResponse;
import com.java.email.model.response.SearchAllCustomerResponse;
import com.java.email.service.CustomerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/customer")
public class CustomerImplController {

    @Autowired
    private CustomerServiceImpl customerService;

    /**
     * 过滤查找客户
     * @param currentUserId 当前用户ID
     * @param currentUserRole 当前用户角色
     * @param filterCustomersDto 过滤条件
     * @return 过滤后的客户列表
     * @throws IOException 如果与 Elasticsearch 交互时出现问题
     */
    @PostMapping("/filter")
    public ResponseEntity<FilterCustomerResponse> filterFindCustomer(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody FilterCustomerDto filterCustomersDto) throws IOException {

        // 调用服务层方法
        FilterCustomerResponse response = customerService.FilterFindCustomers(currentUserId, currentUserRole, filterCustomersDto);

        // 返回成功响应
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/filterAll")
    public ResponseEntity<SearchAllCustomerResponse> filterFindAllCustomer(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody SearchAllCustomerDto searchAllCustomerDto) {

        try {
            // 调用服务层方法获取客户数据
            SearchAllCustomerResponse response = customerService.findFindAllCustomer(currentUserId, currentUserRole, searchAllCustomerDto);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            // 如果发生异常，返回500错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchAllCustomerResponse(0, "Error fetching customer data"));
        }
    }
}
