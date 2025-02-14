package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.receiver.SupplierDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SupplierRepository extends ElasticsearchRepository<SupplierDocument, String> {

    List<SupplierDocument> findByBelongUserId(String oldUserId);
    
}
