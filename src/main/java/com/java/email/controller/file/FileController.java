package com.java.email.controller.file;

import com.java.email.annotation.AuthPermission;
import com.java.email.common.Response.Result;
import com.java.email.constant.AuthConstData;
import com.java.email.service.file.FileService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/file")
//@AuthPermission(permission = AuthConstData.FILE_MANAGE)
public class FileController {
    
    @Autowired
    private FileService fileService;

    @PostMapping("/filterUser")
    public Result filterUser(@RequestBody Map<String, Object> params){
        return fileService.filterUser(params);
    }
    @PostMapping("/filterAdmin")
    public Result filterAdmin(@RequestBody Map<String, Object> params){
        return fileService.filterAdmin(params);
    }

} 