package com.java.email.runner;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import com.java.email.constant.UserConstData;
import com.java.email.esdao.repository.dictionary.EmailTypeRepository;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.model.entity.dictionary.EmailTypeDocument;
import com.java.email.model.entity.user.UserDocument;
import com.java.email.utils.Md5Util;

@Component
@Slf4j
public class DataInitializationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ElasticsearchClient esClient;
    private final EmailTypeRepository emailTypeRepository;
    public DataInitializationRunner(UserRepository userRepository, ElasticsearchClient esClient, EmailTypeRepository emailTypeRepository) {
        this.userRepository = userRepository;
        this.esClient = esClient;
        this.emailTypeRepository = emailTypeRepository;
    }

    @Override
    public void run(String... args) {
        int maxRetries = 10;
        int retryCount = 0;
        int waitTime = 5000; // 5秒

        while (retryCount < maxRetries) {
            try {
                initializeData();
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn("尝试 {} 失败，将在 {} 毫秒后重试", retryCount, waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.error("初始化创建公司、大管理失败，尝试 {} 次后失败", maxRetries);
    }

    private void initializeData() throws Exception {
        // 检查用户表是否为空
        if (userRepository.findAll().iterator().hasNext()) {
            log.info("用户表已存在数据，跳过初始化");
            return;
        }
        log.info("用户表为空，开始初始化数据");
        
        // 创建默认管理员用户
        log.info("开始创建默认公司用户");
        UserDocument company = new UserDocument();
        company.setUserId(UserConstData.COMPANY_USER_ID);
        company.setUserRole(UserConstData.ROLE_COMPANY);
        company.setUserName(UserConstData.COMPANY_USER_NAME);
        company.setUserAccount(UserConstData.COMPANY_USER_ACCOUNT);
        company.setUserPassword("");
        company.setUserEmail("");
        company.setUserEmailCode("");
        company.setUserHost("");
        company.setUserAuthId(Arrays.asList());
        company.setStatus(0);
        company.setBelongUserId("");
        company.setCreatorId("");
        company.setCreatedAt(System.currentTimeMillis()/1000);
        company.setUpdatedAt(System.currentTimeMillis()/1000);
        userRepository.save(company);
        log.info("创建默认公司用户成功");

        // 创建默认测试用户
        log.info("开始创建默认大管理用户");
        UserDocument adminLarge = new UserDocument();
        adminLarge.setUserId(UserConstData.ADMIN_LARGE_USER_ID);
        adminLarge.setUserRole(UserConstData.ROLE_ADMIN_LARGE);
        adminLarge.setUserName(UserConstData.ADMIN_LARGE_USER_NAME);
        adminLarge.setUserAccount(UserConstData.ADMIN_LARGE_USER_ACCOUNT);
        adminLarge.setUserPassword(Md5Util.getMD5String("123456".getBytes()));
        adminLarge.setUserEmail("");
        adminLarge.setUserEmailCode("");
        adminLarge.setUserHost("");
        adminLarge.setUserAuthId(UserConstData.ADMIN_LARGE_AUTH_ID);
        adminLarge.setStatus(0);
        adminLarge.setBelongUserId("");
        adminLarge.setCreatorId("");
        adminLarge.setCreatedAt(System.currentTimeMillis()/1000);
        adminLarge.setUpdatedAt(System.currentTimeMillis()/1000);
        userRepository.save(adminLarge);
        log.info("创建默认大管理用户成功");

        // 检查邮件类型表是否为空
        if (emailTypeRepository.findAll().iterator().hasNext()) {
            log.info("邮件类型表已存在数据，跳过初始化");
            return;
        }
        log.info("邮件类型表为空，开始初始化数据");

        // 创建默认邮件类型
        log.info("开始创建默认生日邮件类型");
        EmailTypeDocument emailType = new EmailTypeDocument();
        emailType.setEmailTypeId(UserConstData.BIRTH_TYPE_ID);
        emailType.setEmailTypeName(UserConstData.BIRTH_TYPE_NAME);
        // 获取当前时间的 ISO 格式字符串
        String now = java.time.Instant.now().toString();
        emailType.setCreatedAt(now);
        emailType.setUpdatedAt(now);
        emailTypeRepository.save(emailType);
        log.info("创建默认生日邮件类型成功");
    }
} 