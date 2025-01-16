package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.EmailTypeRequest;
import com.java.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/dictionary")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/createEmailType")
    public ResponseEntity<Result<HashMap<String, Object>>> createEmail(@RequestBody EmailTypeRequest request) {
        try {
            Result<HashMap<String, Object>> result = new Result<>();
            result.setCode(200);
            result.setMsg("成功");
            result.setData(new HashMap<>());  // 使用空的 HashMap 代替 Object
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result<HashMap<String, Object>> result = new Result<>();
            result.setCode(500);
            result.setMsg(e.getMessage());
            result.setData(new HashMap<>());
            return ResponseEntity.ok(result);
        }
    }
}