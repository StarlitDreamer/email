package com.java.email.service;

import com.java.email.pojo.ResendStrategy;
import com.java.email.pojo.RsendDetails;
import com.java.email.vo.RsendDetailsVo;

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
    Map<String, String> getRecipientDetail(String email,Map<String,String> params);


    /**
     * 从Customer和Supplier索引中查找符合条件的收件人邮箱
     */
    Set<String> findMatchingRecipientEmails(Map<String, String> params);

    /**
     * 从resend_details 索引中查找符合条件的重发邮件id
     */
    RsendDetailsVo findResendDetails (Map<String, String> params);


    RsendDetails getResendDetails(String emailTaskId,String emailId);
} 