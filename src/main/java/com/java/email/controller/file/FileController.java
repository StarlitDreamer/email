package com.java.email.controller.file;

import com.java.email.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/fileManage")
public class FileController {
    
    @Autowired
    private FileService fileService;
    

} 