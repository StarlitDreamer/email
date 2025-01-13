package com.java.email.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.java.email.esdao.AuthDocument;
import com.java.email.repository.AuthRepository;

@Service
public class AuthService {
    @Autowired
    private AuthRepository authRepository;

    public AuthDocument saveAuth(AuthDocument auth) {
        return authRepository.save(auth);
    }
} 