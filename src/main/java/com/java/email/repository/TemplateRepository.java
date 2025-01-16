package com.java.email.repository;

import com.java.email.entity.Template;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TemplateRepository extends ElasticsearchRepository<Template, String> {
}
