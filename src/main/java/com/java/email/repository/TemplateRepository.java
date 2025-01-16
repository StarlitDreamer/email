package com.java.email.repository;

import com.java.email.entity.Template;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TemplateRepository extends ElasticsearchRepository<Template, String> {
    // 根据模板 ID 查找模板
    Template findByTemplateId(String templateId);
}
