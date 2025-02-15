package com.java.email.controller.email;

import com.java.email.common.Response.Result;
import com.java.email.service.email.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
