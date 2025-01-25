package com.java.email;

import com.java.email.service.dictionary.EmailTypeService;
import com.java.email.service.impl.user.AuthService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.java.email.model.entity.dictionary.EmailTypeDocument;
import com.java.email.model.entity.user.AuthDocument;
import com.java.email.constant.AuthConstData;
@SpringBootTest
public class EsTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailTypeService emailTypeService;

    @Test
    public void testSaveAuth() {
        List<AuthDocument> authList = new ArrayList<>();
        
        // Add all auth constants from AuthConstData
        authList.add(createAuth(AuthConstData.MANUAL_SEND));
        authList.add(createAuth(AuthConstData.CIRCLE_SEND)); 
        authList.add(createAuth(AuthConstData.FILE_MANAGE));
        authList.add(createAuth(AuthConstData.EMAIL_TASK_MANAGE));
        authList.add(createAuth(AuthConstData.EMAIL_HISTORY_MANAGE));
        authList.add(createAuth(AuthConstData.EMAIL_TEMPLATE_MANAGE));
        authList.add(createAuth(AuthConstData.TOTAL_REPORT));
        authList.add(createAuth(AuthConstData.TASK_REPORT));
        authList.add(createAuth(AuthConstData.USER_MANAGE));
        authList.add(createAuth(AuthConstData.SUPPLIER_MANAGE));
        authList.add(createAuth(AuthConstData.CUSTOMER_MANAGE));
        authList.add(createAuth(AuthConstData.EMAIL_TYPE_MANAGE));
        authList.add(createAuth(AuthConstData.COMMODITY_MANAGE));
        authList.add(createAuth(AuthConstData.COUNTRY_MANAGE));
        authList.add(createAuth(AuthConstData.AREA_MANAGE));
        authList.add(createAuth(AuthConstData.EMAIL_SERVER_MANAGE));

        for (AuthDocument authDoc : authList) {
            AuthDocument savedAuth = authService.saveAuth(authDoc);
            System.out.println("Saved auth: " + savedAuth.getAuthName());
        }
    }

    @Test
    public void testSaveEmailType() {
        List<EmailTypeDocument> emailTypeList = new ArrayList<>();
        emailTypeList.add(createEmailType("1", "第一个邮件类型"));
        emailTypeList.add(createEmailType("2", "第二个邮件类型"));
        emailTypeList.add(createEmailType("3", "第三个邮件类型"));
        for (EmailTypeDocument emailType : emailTypeList) {
            EmailTypeDocument savedEmailType = emailTypeService.saveEmailType(emailType);
            System.out.println("Saved email type: " + savedEmailType.getEmailTypeName());
        }
    }

    private EmailTypeDocument createEmailType(String emailTypeId, String emailTypeName) {
        EmailTypeDocument emailType = new EmailTypeDocument();
        emailType.setEmailTypeId(emailTypeId);
        emailType.setEmailTypeName(emailTypeName);
        // 获取当前时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String currentTime = LocalDateTime.now().format(formatter);
        emailType.setCreatedAt(currentTime);
        emailType.setUpdatedAt(currentTime);
        return emailType;
    }

    
    private AuthDocument createAuth(String authName) {
        AuthDocument auth = new AuthDocument();
        auth.setAuthName(authName);
        auth.setAuthId(UUID.randomUUID().toString());
        return auth;
    }
}
