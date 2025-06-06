package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.receiver.CustomerDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CustomerRepository extends ElasticsearchRepository<CustomerDocument, String> {

    List<CustomerDocument> findByBelongUserId(String oldUserId);
    
}
