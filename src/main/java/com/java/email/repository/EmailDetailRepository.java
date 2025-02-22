package com.java.email.repository;

import com.java.email.entity.EmailDetail;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface EmailDetailRepository extends ElasticsearchRepository<EmailDetail, String> {
    // 根据状态码筛选状态码为500的邮件
    List<EmailDetail> findByErrorCode(Integer errorCode);

    // 统计送达数量
    long countByErrorCode(Integer errorCode);
}
