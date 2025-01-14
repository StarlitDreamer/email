package com.java.email.esdao.repository.file;

import com.java.email.model.entity.file.AttachmentAssignDocument;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentAssignRepository extends ElasticsearchRepository<AttachmentAssignDocument, String> {
    void save(AttachmentAssignDocument entity);
    Optional<AttachmentAssignDocument> findById(String attachmentId);
}