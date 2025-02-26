package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.model.entity.Template;
import com.java.email.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TemplateService {
    @Autowired
    private TemplateRepository templateRepository;


    @Autowired
    private UserService userService;

    /**
     * 根据模板 ID 查找模板内容并返回 HTML 字符串
     *
     * @param templateId 模板 ID
     * @return HTML 内容字符串
     */
    public String getTemplateContentById(String templateId) {
        // 根据模板 ID 查找模板
        Template template = templateRepository.findByTemplateId(templateId);
        if (template != null) {
            // 返回模板内容
            return template.getTemplateContent();
        } else {
            // 如果模板不存在，返回空字符串或抛出异常
            return "";
        }
    }

    /**
     * 根据任意条件筛选模板
     *
     * @param belongUserId 所属用户ID列表
     * @param creator      创建人
     * @param creatorId    创建人ID
     * @param status       模板状态
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @param page         页码
     * @param size         每页大小
     * @return 符合条件的模板分页结果
     */
    public Result<Page<Template>> findTemplatesByCriteria(
            String currentUserId, // 当前用户的ID
            int currentUserRole,  // 当前用户的角色
            List<String> belongUserId,
            String creator,
            String creatorId,
            Integer status,
            String templateName,
            Integer templateType,
            int page,
            int size) {

        try {
            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 根据用户角色决定查询条件
            if (currentUserRole == 1) { // 公司角色，可以查看所有模板
                return Result.success(templateRepository.findAll(pageable));
            } else if (currentUserRole == 2) { // 大管理角色，可以查看所有模板
                return Result.success(templateRepository.findAll(pageable));
            } else if (currentUserRole == 3) { // 小管理角色，可以查看公司、自己、下属用户的模板
                List<String> allowedUserIds = new ArrayList<>();
                allowedUserIds.add("1"); // 公司用户ID
                allowedUserIds.add(currentUserId); // 自己
                // 假设有一个方法可以获取当前用户的下属用户ID列表
                allowedUserIds.addAll(getSubordinateUserIds(currentUserId));
                return Result.success(templateRepository.findByBelongUserIdIn(allowedUserIds, pageable));
            } else if (currentUserRole == 4) { // 普通用户，只能查看自己的模板
                List<String> allowedUserIds = new ArrayList<>();
                allowedUserIds.add("1"); // 公司用户ID
                allowedUserIds.add(currentUserId); // 自己
                return Result.success(templateRepository.findByBelongUserIdIn(allowedUserIds, pageable));
//                return Result.success(templateRepository.findByBelongUserIdIn(Collections.singletonList(currentUserId), pageable));
            } else {
                // 如果没有匹配的角色，返回空结果
                return Result.success(new PageImpl<>(Collections.emptyList()));
            }
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // 假设有一个方法可以获取当前用户的下属用户ID列表
    private List<String> getSubordinateUserIds(String userId) {
        // 这里实现获取下属用户ID的逻辑
        // 例如：从数据库或ES中查询
        return userService.getSubordinateUserIds(userId);
    }
//    public Result<Page<Template>> findTemplatesByCriteria(
//            List<String> belongUserId, String creator, String creatorId,
//            Integer status, String templateName, Integer templateType,
//            int page, int size) {
//        try {
//            // 创建分页对象
//            Pageable pageable = PageRequest.of(page, size);
//
//            // 动态构建查询条件
//            if (belongUserId != null && !belongUserId.isEmpty()) {
//                return Result.success(templateRepository.findByBelongUserIdIn(belongUserId, pageable));
//            } else if (creator != null) {
//                return Result.success(templateRepository.findByCreator(creator, pageable));
//            } else if (creatorId != null) {
//                return Result.success(templateRepository.findByCreatorId(creatorId, pageable));
//            } else if (status != null) {
//                return Result.success(templateRepository.findByStatus(status, pageable));
//            } else if (templateName != null) {
//                return Result.success(templateRepository.findByTemplateName(templateName, pageable));
//            } else if (templateType != null) {
//                return Result.success(templateRepository.findByTemplateTypeId(templateType, pageable));
//            } else {
//                // 如果没有条件，返回所有模板（分页）
//                return Result.success(templateRepository.findAll(pageable));
//            }
//        } catch (Exception e) {
//            // 返回错误结果
//            return Result.error("查询失败: " + e.getMessage());
//        }
//    }


    /**
     * 分页查询模板数据
     *
     * @param pageNum  当前页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 当前页的数据列表
     */
    /**
     * 分页查询模板数据
     *
     * @param pageNum  当前页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 当前页的数据列表
     */
    public List<Template> getTemplates(
            String currentUserId, // 当前用户的ID
            int currentUserRole,  // 当前用户的角色
            int pageNum,
            int pageSize) {

        // 根据用户角色决定查询条件
        if (currentUserRole == 1 || currentUserRole == 2) { // 公司或大管理角色，可以查看所有模板
            Page<Template> page = templateRepository.findAll(PageRequest.of(pageNum, pageSize));
            return page.getContent();
        } else if (currentUserRole == 3) { // 小管理角色，可以查看公司、自己、下属用户的模板
            List<String> allowedUserIds = new ArrayList<>();
            allowedUserIds.add("1"); // 公司用户ID
            allowedUserIds.add(currentUserId); // 自己
            allowedUserIds.addAll(getSubordinateUserIds(currentUserId)); // 下属用户
            Page<Template> page = templateRepository.findByBelongUserIdIn(allowedUserIds, PageRequest.of(pageNum, pageSize));
            return page.getContent();
        } else if (currentUserRole == 4) { // 普通用户，只能查看自己的模板
            Page<Template> page = templateRepository.findByBelongUserIdIn(Collections.singletonList(currentUserId), PageRequest.of(pageNum, pageSize));
            return page.getContent();
        } else {
            // 如果没有匹配的角色，返回空列表
            return Collections.emptyList();
        }
    }

//    public List<Template> getTemplates(int pageNum, int pageSize) {
//        // 执行分页查询
//        Page<Template> page = templateRepository.findAll(PageRequest.of(pageNum, pageSize));
//        // 获取当前页的数据列表
//        return page.getContent();
//    }
}