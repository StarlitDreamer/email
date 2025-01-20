package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.Attachment;
import com.java.email.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    /**
     * 根据附件 ID 获取附件
     *
     * @param attachmentId 附件 ID
     * @return 附件实体
     */
    @GetMapping("/{attachmentId}")
    public Result getAttachmentById(@PathVariable String attachmentId) {
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        if (attachment != null) {
            return Result.success(attachment);
        } else {
            return Result.error("附件不存在");
        }
    }

    /**
     * 根据条件筛选附件
     *
     * @param belongUserId  所属用户ID列表
     * @param creatorId     创建人ID
     * @param status        附件状态
     * @param attachmentName 附件名称
     * @param page          当前页码
     * @param size          每页大小
     * @return 符合条件的附件列表（分页）
     */
    @GetMapping("/search")
    public Result<Page<Attachment>> findAttachmentsByCriteria(
            @RequestParam(required = false) List<String> belongUserId,
            @RequestParam(required = false) String creatorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String attachmentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("currentUserId") String currentUserId, // 从请求头中获取当前用户ID
            @RequestHeader("currentUserRole") int currentUserRole) { // 从请求头中获取当前用户角色

        return attachmentService.findAttachmentsByCriteria(
                currentUserId, currentUserRole, belongUserId, creatorId, status, attachmentName, page, size);
    }
//    @GetMapping("/search")
//    public Result<Page<Attachment>> findAttachmentsByCriteria(
//            @RequestParam(required = false) List<String> belongUserId,
//            @RequestParam(required = false) String creatorId,
//            @RequestParam(required = false) Integer status,
//            @RequestParam(required = false) String attachmentName,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        return attachmentService.findAttachmentsByCriteria(
//                belongUserId, creatorId, status, attachmentName, page, size);
//    }
}