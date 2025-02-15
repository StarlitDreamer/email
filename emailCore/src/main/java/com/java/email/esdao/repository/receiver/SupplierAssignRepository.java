package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.receiver.SupplierAssignDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface SupplierAssignRepository extends ElasticsearchRepository<SupplierAssignDocument, String> {

    Optional<SupplierAssignDocument> findBySupplierId(String supplierId);

    void deleteBySupplierId(String supplierId);
    
}
