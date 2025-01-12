package com.java.email.service;

import com.java.email.common.Response.Result;
import com.java.email.model.domain.User;

public interface UserLoginService {
    Result login(User user);
}
