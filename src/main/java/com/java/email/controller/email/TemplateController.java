package com.java.email.controller.email;

import com.java.email.annotation.AuthPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.email.constant.AuthConstData;
import com.java.email.service.email.TemplateService;
import com.java.email.common.Response.Result;
import java.util.Map;

@RestController
@RequestMapping("/emailTemplate")
//@AuthPermission(permission = AuthConstData.EMAIL_TEMPLATE_MANAGE)
public class TemplateController {
    
    @Autowired
    private TemplateService templateService;

    @PostMapping("/checkTemplate")
    public Result checkTemplate(@RequestBody Map<String, Object> request) {
        return templateService.checkTemplate(request);
    }

    @PostMapping("/saveTemplate")
    public Result saveTemplate(@RequestBody Map<String, Object> request) {
        return templateService.saveTemplate(request);
    }

    @PostMapping("/assignTemplate")
    public Result assignTemplate(@RequestBody Map<String, Object> request) {
        return templateService.assignTemplate(request);
    }

    @PostMapping("/assignTemplateDetails")
    public Result assignTemplateDetails(@RequestBody Map<String, Object> request) {
        return templateService.assignTemplateDetails(request);
    }

    @PostMapping("/filterTemplate")
    public Result filterTemplate(@RequestBody Map<String, Object> request) {
        return templateService.filterTemplate(request);
    }

    @DeleteMapping("/deleteTemplate")
    public Result deleteTemplate(@RequestBody Map<String, Object> request) {
        return templateService.deleteTemplate(request);
    }
}
