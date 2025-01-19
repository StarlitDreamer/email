package com.java.email.repository;

import com.java.email.entity.UndeliveredEmail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UndeliveredEmailRepository extends ElasticsearchRepository<UndeliveredEmail, String> {

    // 根据邮件任务 ID 查询未送达邮件（分页）
    Page<UndeliveredEmail> findByEmailTaskId(String emailTaskId, Pageable pageable);

    // 根据收件人 ID 列表查询未送达邮件（分页）
    Page<UndeliveredEmail> findByReceiverIdsIn(List<String> receiverIds, Pageable pageable);

    // 根据发件人 ID 列表查询未送达邮件（分页）
    Page<UndeliveredEmail> findBySenderIdsIn(List<String> senderIds, Pageable pageable);

    // 根据重发状态查询未送达邮件（分页）
    Page<UndeliveredEmail> findByResendStatus(int resendStatus, Pageable pageable);

    // 根据错误代码查询未送达邮件（分页）
    Page<UndeliveredEmail> findByErrorCode(Long errorCode, Pageable pageable);

    // 分页查询所有未送达邮件
    Page<UndeliveredEmail> findAll(Pageable pageable);
}