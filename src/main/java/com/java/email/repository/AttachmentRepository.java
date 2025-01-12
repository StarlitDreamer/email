package com.java.email.repository;


import com.java.email.esdao.file.AttachmentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends ElasticsearchRepository<AttachmentDocument, String> {
    // 根据创建者ID查找附件
    List<AttachmentDocument> findByCreatorId(String creatorId);
    
    // 根据附件名称模糊查询
    List<AttachmentDocument> findByAttachmentNameLike(String attachmentName);
    
    // 根据状态查询
    List<AttachmentDocument> findByStatus(Integer status);
} 