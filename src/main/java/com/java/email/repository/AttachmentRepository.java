package com.java.email.repository;

import com.java.email.model.entity.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AttachmentRepository extends ElasticsearchRepository<Attachment, String> {
    // 根据附件 ID 查找附件
    Attachment findByAttachmentId(String attachmentId);

   // 分页查询所有附件
    Page<Attachment> findAll(Pageable pageable);
}