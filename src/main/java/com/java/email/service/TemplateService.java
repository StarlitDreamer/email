package com.java.email.service;

import com.java.email.model.entity.Template;
import com.java.email.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}