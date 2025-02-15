package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.dictionary.CommodityDocument;
import com.java.email.model.entity.receiver.CustomerAssignDocument;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomerAssignRepository extends ElasticsearchRepository<CustomerAssignDocument, String> {

    Optional<CustomerAssignDocument> findByCustomerId(String customerId);

    void deleteByCustomerId(String customerId);
    
}
