package com.java.email.esdao.repository.template;

import com.java.email.model.entity.template.TemplateAssignDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateAssignRepository extends ElasticsearchRepository<TemplateAssignDocument, String> {
} 