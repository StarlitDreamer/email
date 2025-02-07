package com.java.email.controller;

import com.java.email.entity.Test;
import com.java.email.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestService testService;

    // 创建或更新 Test 实体
    @PostMapping
    public Test createOrUpdate(@RequestBody Test test) {
        return testService.save(test);
    }

    // 根据 ID 查询 Test 实体
    @GetMapping("/{id}")
    public Test getById(@PathVariable String id) {
        return testService.findById(id);
    }

    // 获取所有 Test 实体
    @GetMapping
    public Iterable<Test> getAll() {
        return testService.findAll();
    }

    // 根据 ID 删除 Test 实体
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        testService.deleteById(id);
    }

    // 删除所有 Test 实体
    @DeleteMapping
    public void deleteAll() {
        testService.deleteAll();
    }
}

