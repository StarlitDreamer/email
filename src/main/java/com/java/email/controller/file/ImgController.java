package com.java.email.controller.file;

import com.java.email.service.ImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/img")
public class ImgController {
    
    @Autowired
    private ImgService imgService;

} 