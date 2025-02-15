package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.receiver.CustomerAssignDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface CustomerAssignRepository extends ElasticsearchRepository<CustomerAssignDocument, String> {

    Optional<CustomerAssignDocument> findByCustomerId(String customerId);

    void deleteByCustomerId(String customerId);
    
}
