package com.java.email;

import com.java.email.common.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.java.email.esdao.AuthDocument;

@SpringBootTest
public class EsTest {

    @Autowired
    private AuthService authService;
    
    @Test
    public void testSaveAuth() {
        AuthDocument auth = new AuthDocument();
        String authId = java.util.UUID.randomUUID().toString();
        auth.setAuthId(authId);
        auth.setAuthName("邮件服务器管理");

        AuthDocument savedAuth = authService.saveAuth(auth);
        System.out.println("保存成功：" + savedAuth);
    }
}
