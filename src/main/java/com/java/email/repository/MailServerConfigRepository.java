package com.java.email.repository;

import com.java.email.entity.MailServerConfig;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailServerConfigRepository extends ElasticsearchRepository<MailServerConfig, String> {

    // 根据名称查询邮件服务器配置
    List<MailServerConfig> findByName(String name);

    // 根据主机名查询邮件服务器配置
    List<MailServerConfig> findByHost(String host);

    // 根据协议类型查询邮件服务器配置
    List<MailServerConfig> findByProtocol(int protocol);

    // 根据每日限额查询邮件服务器配置
    List<MailServerConfig> findByDailyLimit(int dailyLimit);

    // 根据邮件间隔查询邮件服务器配置
    List<MailServerConfig> findByEmailInterval(int emailInterval);

    // 根据批量发送数量查询邮件服务器配置
    List<MailServerConfig> findByBatchSize(int batchSize);

    // 根据批次间隔查询邮件服务器配置
    List<MailServerConfig> findByBatchInterval(int batchInterval);
}