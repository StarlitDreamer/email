package com.java.email.controller.receiver;

import com.java.email.common.Response.Result;
import com.java.email.service.receiver.ReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
