package com.java.email.repository;

import com.java.email.entity.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface AttachmentRepository extends ElasticsearchRepository<Attachment, String> {
    // 根据附件 ID 查找附件
    Attachment findByAttachmentId(String attachmentId);

    // 根据所属用户 ID 列表查询附件（分页）
    Page<Attachment> findByOwnerUserIdsIn(List<String> ownerUserIds, Pageable pageable);

    // 根据创建人 ID 查询附件（分页）
    Page<Attachment> findByCreatorId(String creatorId, Pageable pageable);

    // 根据状态查询附件（分页）
    Page<Attachment> findByStatus(int status, Pageable pageable);

    // 根据附件名称查询附件（分页）
    Page<Attachment> findByAttachmentName(String attachmentName, Pageable pageable);

    // 分页查询所有附件
    Page<Attachment> findAll(Pageable pageable);
}
