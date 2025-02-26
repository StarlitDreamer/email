package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    /**
     * 根据模板 ID 获取模板内容
     *
     * @param templateId 模板 ID
     * @return HTML 内容字符串
     */
    @GetMapping("/useTemplate/{templateId}")
    public Result getTemplateContent(@PathVariable String templateId) {
        return Result.success(templateService.getTemplateContentById(templateId));
    }
}