package com.java.email.service;

import com.java.email.entity.Attachment;
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

//    /**
//     * 根据条件筛选附件
//     *
//     * @param belongUserId   所属用户ID列表
//     * @param creatorId      创建人ID
//     * @param status         附件状态
//     * @param attachmentName 附件名称
//     * @param page           当前页码
//     * @param size           每页大小
//     * @return 符合条件的附件列表（分页）
//     */
//    public Result<Page<Attachment>> findAttachmentsByCriteria(
//            String currentUserId, // 当前用户的ID
//            int currentUserRole,  // 当前用户的角色
//            List<String> belongUserId,
//            String creatorId,
//            Integer status,
//            String attachmentName,
//            int page,
//            int size) {
//
//        try {
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 根据用户角色决定查询条件
//            if (currentUserRole == 1) { // 公司角色，可以查看所有附件
//                return Result.success(attachmentRepository.findAll(pageable));
//            } else if (currentUserRole == 2) { // 大管理角色，可以查看所有附件
//                return Result.success(attachmentRepository.findAll(pageable));
//            } else if (currentUserRole == 3) { // 小管理角色，可以查看公司、自己、下属用户的附件
//                List<String> allowedUserIds = new ArrayList<>();
//                allowedUserIds.add("1"); // 公司用户ID
//                allowedUserIds.add(currentUserId); // 自己
//                // 假设有一个方法可以获取当前用户的下属用户ID列表
//                allowedUserIds.addAll(getSubordinateUserIds(currentUserId));
//                return Result.success(attachmentRepository.findByBelongUserIdIn(allowedUserIds, pageable));
//            } else if (currentUserRole == 4) { // 普通用户，只能查看自己的附件
//                return Result.success(attachmentRepository.findByBelongUserIdIn(Collections.singletonList(currentUserId), pageable));
//            } else {
//                // 如果没有匹配的角色，返回空结果
//                return Result.success(new PageImpl<>(Collections.emptyList()));
//            }
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询失败: " + e.getMessage());
//        }
//    }
//
//    // 假设有一个方法可以获取当前用户的下属用户ID列表
//    private List<String> getSubordinateUserIds(String userId) {
//        // 这里实现获取下属用户ID的逻辑
//        // 例如：从数据库或ES中查询
//        return userService.getSubordinateUserIds(userId);
//    }
//    public Result<Page<Attachment>> findAttachmentsByCriteria(
//            List<String> belongUserId, String creatorId, Integer status,
//            String attachmentName, int page, int size) {
//        try {
//            Page<Attachment> attachments;
//
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (belongUserId != null && !belongUserId.isEmpty()) {
//                attachments = attachmentRepository.findByBelongUserIdIn(belongUserId, pageable);
//            } else if (creatorId != null) {
//                attachments = attachmentRepository.findByCreatorId(creatorId, pageable);
//            } else if (status != null) {
//                attachments = attachmentRepository.findByStatus(status, pageable);
//            } else if (attachmentName != null) {
//                attachments = attachmentRepository.findByAttachmentName(attachmentName, pageable);
//            } else {
//                // 如果没有条件，返回所有附件（分页）
//                attachments = attachmentRepository.findAll(pageable);
//            }
//
//            // 返回成功结果
//            return Result.success(attachments);
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询失败: " + e.getMessage());
//        }
//    }
}