package com.java.email.service;

import com.java.email.model.entity.Attachment;
import com.java.email.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttachmentService {
    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private UserService userService;

    /**
     * 根据附件 ID 查找附件
     *
     * @param attachmentId 附件 ID
     * @return 附件实体
     */
    public Attachment getAttachmentById(String attachmentId) {
        return attachmentRepository.findByAttachmentId(attachmentId);
    }
}