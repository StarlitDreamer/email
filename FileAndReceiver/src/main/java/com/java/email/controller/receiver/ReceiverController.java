package com.java.email.controller.receiver;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.java.email.common.Response.Result;
import com.java.email.service.receiver.ReceiverService;

@RestController
@RequestMapping("/receiverManage")
public class ReceiverController {

    @Autowired
    private ReceiverService receiverService;

    @PostMapping("/filterUser")
    public Result filterUser(@RequestBody Map<String, Object> request) {
        return receiverService.filterUser(request);
    }

    @PostMapping("/changeBelongUser")
    public Result changeBelongUser(@RequestBody Map<String, Object> request) {
        return receiverService.changeBelongUser(request);
    }

    @GetMapping("/getCategory")
    public Result getCategory() {
        return receiverService.getCategory();
    }

    @PostMapping("/filterCommodity")
    public Result filterCommodity(@RequestBody Map<String, Object> request) {
        return receiverService.filterCommodity(request);
    }
}
