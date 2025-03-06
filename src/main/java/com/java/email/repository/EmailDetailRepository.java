package com.java.email.repository;

import com.java.email.model.entity.EmailDetail;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface EmailDetailRepository extends ElasticsearchRepository<EmailDetail, String> {
    // 根据状态码筛选状态码为500的邮件
    List<EmailDetail> findByErrorCode(Integer errorCode);

    // 统计送达数量
    long countByErrorCode(Integer errorCode);

    // 根据 emailTaskId 和 errorCode 查询 EmailDetail 列表
    List<EmailDetail> findByEmailTaskIdAndErrorCode(String emailTaskId, Integer errorCode);

    // 根据 emailTaskId 查询所有 EmailDetail 列表
    EmailDetail findByEmailId(String emailId);

    EmailDetail findByEmailTaskIdAndReceiverId(String emailTaskId, String receiverId);
}
