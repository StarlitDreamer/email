package com.java.email.service.impl.user;

import com.java.email.esdao.repository.user.AuthRepository;
import com.java.email.model.entity.user.AuthDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private AuthRepository authRepository;

    public AuthDocument saveAuth(AuthDocument auth) {
        return authRepository.save(auth);
    }
} 