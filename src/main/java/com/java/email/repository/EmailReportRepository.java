package com.java.email.repository;

import com.java.email.model.entity.EmailReport;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface EmailReportRepository extends ElasticsearchRepository<EmailReport, String> {

    Optional<EmailReport> findByEmailTaskId(String emailTaskId);  // 根据emailTaskId查询EmailReport
}
