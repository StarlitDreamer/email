package com.java.email;

import com.java.email.common.AuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.java.email.model.entity.AuthDocument;
import com.java.email.constant.AuthConstData;
@SpringBootTest
public class EsTest {

    @Autowired
    private AuthService authService;
    
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


    private AuthDocument createAuth(String authName) {
        AuthDocument auth = new AuthDocument();
        auth.setAuthName(authName);
        auth.setAuthId(UUID.randomUUID().toString());
        return auth;
    }
}
