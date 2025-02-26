package com.java.email.controller;

import com.java.email.dto.FilterCustomersDto;
import com.java.email.dto.FilterCustomersResponse;
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
    public ResponseEntity<FilterCustomersResponse> filterFindCustomers(
            @RequestHeader String currentUserId,
            @RequestHeader int currentUserRole,
            @RequestBody FilterCustomersDto filterCustomersDto) throws IOException {
        System.out.println("1111");
        // 调用服务层方法
        FilterCustomersResponse response = customerService.FilterFindCustomers(currentUserId, currentUserRole, filterCustomersDto);

        // 返回成功响应
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
