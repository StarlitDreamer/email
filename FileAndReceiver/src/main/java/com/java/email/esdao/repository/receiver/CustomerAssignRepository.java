package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.receiver.CustomerAssignDocument;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomerAssignRepository extends ElasticsearchRepository<CustomerAssignDocument, String> {
    
}
