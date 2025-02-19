package com.java.email.service.impl;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.pojo.Supplier;
import com.java.email.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.ArrayList;

@Service
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "supplier";
    
    @Autowired
    public SupplierServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }
    
    @Override
    public Supplier saveSupplier(Supplier supplier) {
        try {
            // 设置创建和更新时间
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (supplier.getCreatedAt() == null) {
                supplier.setCreatedAt(currentTime);
            }
            supplier.setUpdatedAt(currentTime);
            
            // 如果没有ID，生成新ID
            if (supplier.getSupplierid() == null) {
                supplier.setSupplierid(UUID.randomUUID().toString());
            }
            
            IndexResponse response = esClient.index(i -> i
                .index(INDEX_NAME)
                .id(supplier.getSupplierid())
                .document(supplier)
            );
            
            if (response.result().name().equals("Created") || 
                response.result().name().equals("Updated")) {
                return supplier;
            }
            
            log.error("保存供应商信息失败: response={}", response);
            return null;
            
        } catch (Exception e) {
            log.error("保存供应商信息异常: supplier={}, error={}", supplier, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public Supplier findById(String supplierId) {
        try {
            GetResponse<Supplier> response = esClient.get(g -> g
                .index(INDEX_NAME)
                .id(supplierId),
                Supplier.class
            );
            
            return response.found() ? response.source() : null;
            
        } catch (Exception e) {
            log.error("查询供应商失败: supplierId={}, error={}", supplierId, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<Supplier> findByEmail(String email) {
        try {
            SearchResponse<Supplier> response = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                    .terms(t -> t
                        .field("emails")
                        .terms(ft -> ft.value(Collections.singletonList(FieldValue.of(email))))
                    )
                ),
                Supplier.class
            );
            
            return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("根据邮箱查询供应商失败: email={}, error={}", email, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    

} 