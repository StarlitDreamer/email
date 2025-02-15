package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.dictionary.CommodityDocument;
import com.java.email.model.entity.receiver.CustomerDocument;

import java.util.List;
import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomerRepository extends ElasticsearchRepository<CustomerDocument, String> {

    List<CustomerDocument> findByBelongUserId(String oldUserId);

    Optional<CustomerDocument> findByCustomerId(String customerId);

    void deleteByCustomerId(String customerId);
    
}
