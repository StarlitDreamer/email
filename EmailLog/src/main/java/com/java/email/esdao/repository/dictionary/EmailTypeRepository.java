package com.java.email.esdao.repository.dictionary;

import com.java.email.model.entity.dictionary.EmailTypeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EmailTypeRepository extends ElasticsearchRepository<EmailTypeDocument, String> {


    
}