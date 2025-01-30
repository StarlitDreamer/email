package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.MailServerConfig;
import com.java.email.entity.MailServerProperties;
import com.java.email.service.MailServerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mail-server-config")
public class MailServerConfigController {

    @Autowired
    private MailServerConfigService mailServerConfigService;

    @PostMapping
    public Result<MailServerConfig> saveOrUpdate(@RequestBody MailServerConfig config) {
        return mailServerConfigService.saveOrUpdate(config);
    }

    @GetMapping("/{id}")
    public Result<MailServerConfig> findById(@PathVariable String id) {
        return mailServerConfigService.findById(id);
    }

    @GetMapping("/name/{name}")
    public Result<List<MailServerConfig>> findByName(@PathVariable String name) {
        return mailServerConfigService.findByName(name);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable String id) {
        return mailServerConfigService.deleteById(id);
    }

    @GetMapping
    public Result<Page<MailServerConfig>> findAll(@RequestParam int page, @RequestParam int size) {
        return mailServerConfigService.findAll(page, size);
    }

    @PutMapping("/{id}")
    public Result<MailServerConfig> updateConfig(
            @PathVariable String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer dailyLimit,
            @RequestParam(required = false) Integer emailInterval,
            @RequestParam(required = false) Integer batchSize,
            @RequestParam(required = false) Integer batchInterval) {
        return mailServerConfigService.updateConfig(id, name, dailyLimit, emailInterval, batchSize, batchInterval);
    }

    @PostMapping("/add")
    public Result<MailServerConfig> addConfig(
            @RequestParam String name,
            @RequestParam String host,
            @RequestParam int port,
            @RequestParam String username,
            @RequestParam String encryptedPassword,
            @RequestParam int protocol,
            @RequestBody MailServerProperties properties,
            @RequestParam int dailyLimit,
            @RequestParam int emailInterval,
            @RequestParam int batchSize,
            @RequestParam int batchInterval) {
        return mailServerConfigService.addConfig(
                name, host, port, username, encryptedPassword, protocol,
                properties, dailyLimit, emailInterval, batchSize, batchInterval);
    }
}