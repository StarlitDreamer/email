package com.java.email.repository;

import com.java.email.model.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TemplateRepository extends ElasticsearchRepository<Template, String> {
    // 根据模板 ID 查找模板
    Template findByTemplateId(String templateId);

    // 分页查询所有模板
    Page<Template> findAll(Pageable pageable);
}