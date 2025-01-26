package com.java.email.controller;

import com.java.email.entity.MailServerConfig;
import com.java.email.service.MailServerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/mail-server-config")
public class MailServerConfigController {

    @Autowired
    private MailServerConfigService mailServerConfigService;

    // 获取所有配置
    @GetMapping
    public List<MailServerConfig> getAllConfigs() {
        return mailServerConfigService.findAll();
    }

    // 根据ID获取配置
    @GetMapping("/{id}")
    public Optional<MailServerConfig> getConfigById(@PathVariable String id) {
        return mailServerConfigService.findById(id);
    }

    // 创建或更新配置
    @PostMapping
    public MailServerConfig createOrUpdateConfig(@RequestBody MailServerConfig config) {
        return mailServerConfigService.saveOrUpdate(config);
    }

    // 根据ID删除配置
    @DeleteMapping("/{id}")
    public void deleteConfig(@PathVariable String id) {
        mailServerConfigService.deleteById(id);
    }

    // 根据名称查找配置
    @GetMapping("/search")
    public List<MailServerConfig> searchByName(@RequestParam String name) {
        return mailServerConfigService.findByName(name);
    }
}