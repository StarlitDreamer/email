package com.java.email.repository;

import com.java.email.entity.Test;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TestRepository extends ElasticsearchRepository<Test, String> {
    // 你可以在这里添加自定义查询方法
}
