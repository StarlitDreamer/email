package com.java.email.repository;

import com.java.email.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface TemplateRepository extends ElasticsearchRepository<Template, String> {
    // 根据模板 ID 查找模板
    Template findByTemplateId(String templateId);

    // 根据所属用户 ID 列表查询模板（分页）
    Page<Template> findByBelongUserIdIn(List<String> belongUserId, Pageable pageable);

    // 根据创建人查询模板（分页）
    Page<Template> findByCreator(String creator, Pageable pageable);

    // 根据创建人 ID 查询模板（分页）
    Page<Template> findByCreatorId(String creatorId, Pageable pageable);

    // 根据状态查询模板（分页）
    Page<Template> findByStatus(int status, Pageable pageable);

    // 根据模板名称查询模板（分页）
    Page<Template> findByTemplateName(String templateName, Pageable pageable);

    // 根据模板类型查询模板（分页）
    Page<Template> findByTemplateTypeId(int templateTypeId, Pageable pageable);

    // 分页查询所有模板
    Page<Template> findAll(Pageable pageable);
}