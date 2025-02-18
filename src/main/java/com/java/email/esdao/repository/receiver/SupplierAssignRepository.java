package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.receiver.SupplierAssignDocument;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SupplierAssignRepository extends ElasticsearchRepository<SupplierAssignDocument, String> {
    
}
