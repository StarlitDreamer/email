package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.Template;
import com.java.email.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
     * @param ownerUserIds 所属用户ID列表
     * @param creator      创建人
     * @param creatorId    创建人ID
     * @param status       模板状态
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @return 符合条件的模板列表
     */

    public Result<List<Template>> findTemplatesByCriteria(List<String> ownerUserIds, String creator, String creatorId,
                                                          Integer status, String templateName, Integer templateType) {
        try {
            List<Template> templates;

            // 动态构建查询条件
            if (ownerUserIds != null && !ownerUserIds.isEmpty()) {
                templates = templateRepository.findByOwnerUserIdsIn(ownerUserIds);
            } else if (creator != null) {
                templates = templateRepository.findByCreator(creator);
            } else if (creatorId != null) {
                templates = templateRepository.findByCreatorId(creatorId);
            } else if (status != null) {
                templates = templateRepository.findByStatus(status);
            } else if (templateName != null) {
                templates = templateRepository.findByTemplateName(templateName);
            } else if (templateType != null) {
                templates = templateRepository.findByTemplateType(templateType);
            } else {
                // 如果没有条件，返回所有模板
                templates = (List<Template>) templateRepository.findAll();
            }

            // 返回成功结果
            return Result.success(templates);
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
//    public List<Template> findTemplatesByCriteria(List<String> ownerUserIds, String creator, String creatorId,
//                                                  Integer status, String templateName, Integer templateType) {
//        // 动态构建查询条件
//        if (ownerUserIds != null && !ownerUserIds.isEmpty()) {
//            return templateRepository.findByOwnerUserIdsIn(ownerUserIds);
//        }
//        if (creator != null) {
//            return templateRepository.findByCreator(creator);
//        }
//        if (creatorId != null) {
//            return templateRepository.findByCreatorId(creatorId);
//        }
//        if (status != null) {
//            return templateRepository.findByStatus(status);
//        }
//        if (templateName != null) {
//            return templateRepository.findByTemplateName(templateName);
//        }
//        if (templateType != null) {
//            return templateRepository.findByTemplateType(templateType);
//        }
//        // 如果没有条件，返回所有模板
//        return (List<Template>) templateRepository.findAll();
//    }
}
