package com.java.email.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EmailRecipientService {

    
    /**
     * 获取收件人详情
     * @param email 邮箱
     * @return map
     */
    Map<String, String> getRecipientDetail(String email);

    /**
     * 从Customer和Supplier索引中查找符合条件的收件人邮箱
     */
    Set<String> findMatchingRecipientEmails(Map<String, String> params);
} 