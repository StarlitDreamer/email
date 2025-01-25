package com.java.email.esdao.repository.template;

import com.java.email.model.entity.template.TemplateDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends ElasticsearchRepository<TemplateDocument, String> {
} 