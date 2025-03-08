package com.java.email.repository;

import com.java.email.model.entity.EmailContent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EmailContentRepository extends ElasticsearchRepository<EmailContent, String> {

}