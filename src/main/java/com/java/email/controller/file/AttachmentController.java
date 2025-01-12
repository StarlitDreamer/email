package com.java.email.controller.file;

import com.java.email.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {
    
    @Autowired
    private AttachmentService attachmentService;

} 