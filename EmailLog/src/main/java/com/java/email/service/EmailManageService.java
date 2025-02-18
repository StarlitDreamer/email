package com.java.email.service;

import com.java.email.pojo.EmailManage;

import java.util.List;

public interface EmailManageService {
    
    /**
     * 根据邮件任务ID查询邮件状态
     * @param emailTaskId 邮件任务ID
     * @return 邮件状态列表
     */
    List<EmailManage> findByEmailTaskId(String emailTaskId);

    Long findLatestStatusByTaskId(String emailTaskId);
}