package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Attachment;
import com.java.email.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttachmentService {
    @Autowired
    private AttachmentRepository attachmentRepository;

    /**
     * 根据附件 ID 查找附件
     *
     * @param attachmentId 附件 ID
     * @return 附件实体
     */
    public Attachment getAttachmentById(String attachmentId) {
        return attachmentRepository.findByAttachmentId(attachmentId);
    }

    /**
     * 根据条件筛选附件
     *
     * @param ownerUserIds  所属用户ID列表
     * @param creatorId     创建人ID
     * @param status        附件状态
     * @param attachmentName 附件名称
     * @param page          当前页码
     * @param size          每页大小
     * @return 符合条件的附件列表（分页）
     */
    public Result<Page<Attachment>> findAttachmentsByCriteria(
            List<String> ownerUserIds, String creatorId, Integer status,
            String attachmentName, int page, int size) {
        try {
            Page<Attachment> attachments;

            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 动态构建查询条件
            if (ownerUserIds != null && !ownerUserIds.isEmpty()) {
                attachments = attachmentRepository.findByOwnerUserIdsIn(ownerUserIds, pageable);
            } else if (creatorId != null) {
                attachments = attachmentRepository.findByCreatorId(creatorId, pageable);
            } else if (status != null) {
                attachments = attachmentRepository.findByStatus(status, pageable);
            } else if (attachmentName != null) {
                attachments = attachmentRepository.findByAttachmentName(attachmentName, pageable);
            } else {
                // 如果没有条件，返回所有附件（分页）
                attachments = attachmentRepository.findAll(pageable);
            }

            // 返回成功结果
            return Result.success(attachments);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}