package com.java.email.controller.file;

import com.java.email.common.Response.Result;
import com.java.email.service.file.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file/attachment")
//@AuthPermission(permission = AuthConstData.FILE_MANAGE)
public class AttachmentController {
    
    @Autowired
    private AttachmentService attachmentService;

    @PostMapping("/uploadAttachment")
    public Result uploadAttachment(@RequestBody Map<String, List<Map<String, String>>> request) {
        return attachmentService.uploadAttachment(request);
    }

    @PostMapping("/assignAttachment")
    public Result assignAttachment(@RequestBody Map<String, Object> request) {
        return attachmentService.assignAttachment(request);
    }

    @PostMapping("/assignAttachmentDetails")
    public Result getAssignAttachmentDetails(@RequestBody Map<String, Object> request) {
        return attachmentService.assignAttachmentDetails(request);
    }

    @DeleteMapping("/deleteAttachment")
    public Result deleteAttachment(@RequestBody Map<String, Object> request) {
        return attachmentService.deleteAttachment(request);
    }

    @PostMapping("/filterAttachment")
    public Result filterAttachment(@RequestBody Map<String, Object> request) {
        return attachmentService.filterAttachment(request);
    }

    
} 
