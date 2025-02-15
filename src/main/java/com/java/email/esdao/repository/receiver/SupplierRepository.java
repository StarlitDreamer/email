package com.java.email.esdao.repository.receiver;

import com.java.email.model.entity.dictionary.CommodityDocument;
import com.java.email.model.entity.receiver.SupplierDocument;

import java.util.List;
import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SupplierRepository extends ElasticsearchRepository<SupplierDocument, String> {

    List<SupplierDocument> findByBelongUserId(String oldUserId);

    Optional<SupplierDocument> findBySupplierId(String supplierId);

    void deleteBySupplierId(String supplierId);
    
}
