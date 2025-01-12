package com.java.email.repository;

import com.java.email.esdao.file.AttachmentAssignDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentAssignRepository extends ElasticsearchRepository<AttachmentAssignDocument, String> {
    // 基础的CRUD操作由ElasticsearchRepository提供
}