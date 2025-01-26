package com.java.email.service;

import com.java.email.entity.MailServerConfig;
import com.java.email.repository.MailServerConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MailServerConfigService {

    @Autowired
    private MailServerConfigRepository mailServerConfigRepository;

    // 保存或更新配置
    public MailServerConfig saveOrUpdate(MailServerConfig config) {
        return mailServerConfigRepository.save(config);
    }

    // 根据ID查找配置
    public Optional<MailServerConfig> findById(String id) {
        return mailServerConfigRepository.findById(id);
    }

    // 查找所有配置
    public List<MailServerConfig> findAll() {
        return (List<MailServerConfig>) mailServerConfigRepository.findAll();
    }

    // 根据ID删除配置
    public void deleteById(String id) {
        mailServerConfigRepository.deleteById(id);
    }

    // 根据名称查找配置
    public List<MailServerConfig> findByName(String name) {
        return mailServerConfigRepository.findByName(name);
    }
}
