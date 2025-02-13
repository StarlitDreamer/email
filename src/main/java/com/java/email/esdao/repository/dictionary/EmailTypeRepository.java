package com.java.email.esdao.repository.dictionary;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.java.email.model.entity.dictionary.EmailTypeDocument;

public interface EmailTypeRepository extends ElasticsearchRepository<EmailTypeDocument, String> {


    
}