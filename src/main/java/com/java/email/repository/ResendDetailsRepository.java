package com.java.email.repository;

import com.java.email.model.entity.ResendDetails;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ResendDetailsRepository extends ElasticsearchRepository<ResendDetails, String> {
    // 可以添加自定义查询方法
}
