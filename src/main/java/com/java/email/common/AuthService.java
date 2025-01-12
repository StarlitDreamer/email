package com.java.email.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.java.email.esdao.AuthDocument;
import com.java.email.repository.AuthRepository;
import com.java.email.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthRepository authRepository;
    
    @Override
    public AuthDocument saveAuth(AuthDocument auth) {
        return authRepository.save(auth);
    }
} 