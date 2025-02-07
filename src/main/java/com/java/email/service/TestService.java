package com.java.email.service;

import com.java.email.entity.Test;
import com.java.email.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

    // 创建或更新 Test 实体
    public Test save(Test test) {
        return testRepository.save(test);
    }

    // 根据 ID 查询 Test 实体
    public Test findById(String id) {
        return testRepository.findById(id).orElse(null);  // 如果没有找到，返回 null
    }

    // 获取所有 Test 实体
    public Iterable<Test> findAll() {
        return testRepository.findAll();
    }

    // 根据 ID 删除 Test 实体
    public void deleteById(String id) {
        testRepository.deleteById(id);
    }

    // 删除所有 Test 实体
    public void deleteAll() {
        testRepository.deleteAll();
    }
}

