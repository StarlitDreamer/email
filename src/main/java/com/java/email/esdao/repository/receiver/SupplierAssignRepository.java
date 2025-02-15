package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.dictionary.CommodityDocument;
import com.java.email.model.entity.receiver.SupplierAssignDocument;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SupplierAssignRepository extends ElasticsearchRepository<SupplierAssignDocument, String> {

    Optional<SupplierAssignDocument> findBySupplierId(String supplierId);

    void deleteBySupplierId(String supplierId);
    
}
