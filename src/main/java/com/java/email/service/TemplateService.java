package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Template;
import com.java.email.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {
    @Autowired
    private TemplateRepository templateRepository;

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
            List<String> belongUserId, String creator, String creatorId,
            Integer status, String templateName, Integer templateType,
            int page, int size) {
        try {
            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 动态构建查询条件
            if (belongUserId != null && !belongUserId.isEmpty()) {
                return Result.success(templateRepository.findByBelongUserIdIn(belongUserId, pageable));
            } else if (creator != null) {
                return Result.success(templateRepository.findByCreator(creator, pageable));
            } else if (creatorId != null) {
                return Result.success(templateRepository.findByCreatorId(creatorId, pageable));
            } else if (status != null) {
                return Result.success(templateRepository.findByStatus(status, pageable));
            } else if (templateName != null) {
                return Result.success(templateRepository.findByTemplateName(templateName, pageable));
            } else if (templateType != null) {
                return Result.success(templateRepository.findByTemplateTypeId(templateType, pageable));
            } else {
                // 如果没有条件，返回所有模板（分页）
                return Result.success(templateRepository.findAll(pageable));
            }
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询模板数据
     *
     * @param pageNum  当前页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 当前页的数据列表
     */
    public List<Template> getTemplates(int pageNum, int pageSize) {
        // 执行分页查询
        Page<Template> page = templateRepository.findAll(PageRequest.of(pageNum, pageSize));
        // 获取当前页的数据列表
        return page.getContent();
    }
}