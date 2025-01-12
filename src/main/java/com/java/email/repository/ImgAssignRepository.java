package com.java.email.repository;

import com.java.email.esdao.file.ImgAssignDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImgAssignRepository extends ElasticsearchRepository<ImgAssignDocument, String> {
    // 基础的CRUD操作由ElasticsearchRepository提供
} 