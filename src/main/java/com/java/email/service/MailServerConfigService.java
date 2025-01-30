package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.MailServerConfig;
import com.java.email.entity.MailServerProperties;
import com.java.email.repository.MailServerConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MailServerConfigService {

    @Autowired
    private MailServerConfigRepository mailServerConfigRepository;

    /**
     * 保存或更新邮件服务器配置
     *
     * @param mailServerConfig 邮件服务器配置
     * @return 保存或更新后的邮件服务器配置
     */
    public Result<MailServerConfig> saveOrUpdate(MailServerConfig mailServerConfig) {
        try {
            MailServerConfig savedConfig = mailServerConfigRepository.save(mailServerConfig);
            return Result.success(savedConfig);
        } catch (Exception e) {
            return Result.error("保存或更新失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询邮件服务器配置
     *
     * @param id 邮件服务器配置ID
     * @return 邮件服务器配置
     */
    public Result<MailServerConfig> findById(String id) {
        try {
            Optional<MailServerConfig> config = mailServerConfigRepository.findById(id);
            return config.map(Result::success).orElseGet(() -> Result.error("未找到对应的邮件服务器配置"));
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据名称查询邮件服务器配置
     *
     * @param name 邮件服务器配置名称
     * @return 邮件服务器配置列表
     */
    public Result<List<MailServerConfig>> findByName(String name) {
        try {
            List<MailServerConfig> configs = mailServerConfigRepository.findByName(name);
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据主机名查询邮件服务器配置
     *
     * @param host 邮件服务器主机名
     * @return 邮件服务器配置列表
     */
    public Result<List<MailServerConfig>> findByHost(String host) {
        try {
            List<MailServerConfig> configs = mailServerConfigRepository.findByHost(host);
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据协议类型查询邮件服务器配置
     *
     * @param protocol 协议类型
     * @return 邮件服务器配置列表
     */
    public Result<List<MailServerConfig>> findByProtocol(int protocol) {
        try {
            List<MailServerConfig> configs = mailServerConfigRepository.findByProtocol(protocol);
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除邮件服务器配置
     *
     * @param id 邮件服务器配置ID
     * @return 删除结果
     */
    public Result<Void> deleteById(String id) {
        try {
            mailServerConfigRepository.deleteById(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询所有邮件服务器配置
     *
     * @param page 页码
     * @param size 每页大小
     * @return 邮件服务器配置分页结果
     */
    public Result<Page<MailServerConfig>> findAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<MailServerConfig> configs = mailServerConfigRepository.findAll(pageable);
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新邮件服务器配置
     *
     * @param id              邮件服务器配置ID
     * @param name            服务器名称
     * @param dailyLimit      每日限额
     * @param emailInterval   邮件间隔（秒）
     * @param batchSize       批量发送数量
     * @param batchInterval   批次间隔（分钟）
     * @return 更新后的邮件服务器配置
     */
    public Result<MailServerConfig> updateConfig(
            String id,
            String name,
            Integer dailyLimit,
            Integer emailInterval,
            Integer batchSize,
            Integer batchInterval) {
        try {
            // 根据ID查询现有的配置
            Optional<MailServerConfig> optionalConfig = mailServerConfigRepository.findById(id);
            if (!optionalConfig.isPresent()) {
                return Result.error("未找到对应的邮件服务器配置");
            }

            // 获取现有的配置
            MailServerConfig config = optionalConfig.get();

            // 更新配置字段
            if (name != null) {
                config.setName(name);
            }
            if (dailyLimit != null) {
                config.setDailyLimit(dailyLimit);
            }
            if (emailInterval != null) {
                config.setEmailInterval(emailInterval);
            }
            if (batchSize != null) {
                config.setBatchSize(batchSize);
            }
            if (batchInterval != null) {
                config.setBatchInterval(batchInterval);
            }

            // 保存更新后的配置
            MailServerConfig updatedConfig = mailServerConfigRepository.save(config);
            return Result.success(updatedConfig);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 添加新的邮件服务器配置
     *
     * @param name            服务器名称
     * @param host            服务器主机
     * @param port            服务器端口
     * @param username        用户名
     * @param encryptedPassword 加密后的密码
     * @param protocol        协议类型（0：SMTP, 1：IMAP, 2：POP3）
     * @param properties      邮件服务器属性
     * @param dailyLimit      每日限额
     * @param emailInterval   邮件间隔（秒）
     * @param batchSize       批量发送数量
     * @param batchInterval   批次间隔（分钟）
     * @return 新添加的邮件服务器配置
     */
    public Result<MailServerConfig> addConfig(
            String name,
            String host,
            int port,
            String username,
            String encryptedPassword,
            int protocol,
            MailServerProperties properties,
            int dailyLimit,
            int emailInterval,
            int batchSize,
            int batchInterval) {
        try {
            // 创建新的邮件服务器配置对象
            MailServerConfig config = new MailServerConfig();
            config.setName(name);
            config.setHost(host);
            config.setPort(port);
            config.setUsername(username);
            config.setEncryptedPassword(encryptedPassword);
            config.setProtocol(protocol);
            config.setProperties(properties);
            config.setDailyLimit(dailyLimit);
            config.setEmailInterval(emailInterval);
            config.setBatchSize(batchSize);
            config.setBatchInterval(batchInterval);

            // 保存配置
            MailServerConfig savedConfig = mailServerConfigRepository.save(config);
            return Result.success(savedConfig);
        } catch (Exception e) {
            return Result.error("添加失败: " + e.getMessage());
        }
    }
}