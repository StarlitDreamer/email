package com.java.email.repository;

import com.java.email.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TemplateRepository extends ElasticsearchRepository<Template, String> {
    // 根据模板 ID 查找模板
    Template findByTemplateId(String templateId);

    // 自定义查询方法
    List<Template> findByOwnerUserIdsIn(List<String> ownerUserIds);
    List<Template> findByCreator(String creator);
    List<Template> findByCreatorId(String creatorId);
    List<Template> findByStatus(int status);
    List<Template> findByTemplateName(String templateName);
    List<Template> findByTemplateType(int templateType);

    Page<Template> findAll(Pageable pageable); // 分页查询方法
}
