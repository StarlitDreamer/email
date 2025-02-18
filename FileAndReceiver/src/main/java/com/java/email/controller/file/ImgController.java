package com.java.email.controller.file;

import com.java.email.annotation.AuthPermission;
import com.java.email.common.Response.Result;
import com.java.email.constant.AuthConstData;
import com.java.email.service.file.ImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file/img")
//@AuthPermission(permission = AuthConstData.FILE_MANAGE)
public class ImgController {

    @Autowired
    private ImgService imgService;

    @PostMapping("/uploadImg")
    public Result uploadImg(@RequestBody Map<String, List<Map<String, String>>> request) {
        return imgService.uploadImg(request);
    }

    @PostMapping("/assignImg")
    public Result assignImg(@RequestBody Map<String, Object> request) {
        return imgService.assignImg(request);
    }

    @PostMapping("/assignImgDetails")
    public Result assignImgDetails(@RequestBody Map<String, Object> request) {
        return imgService.assignImgDetails(request);
    }

    @DeleteMapping("/deleteImg")
    public Result deleteImg(@RequestBody Map<String, Object> request) {
        return imgService.deleteImg(request);
    }

    @PostMapping("/filterImg")
    public Result filterImg(@RequestBody Map<String, Object> request) {
        return imgService.filterImg(request);
    }
} 