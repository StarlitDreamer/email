package com.java.email.service;



import com.java.email.pojo.User;

import java.io.IOException;

public interface UserService {

    void saveUser(User user) throws IOException;
    User findById(String id) throws IOException;
    User findByUserId(String userId) throws IOException;

}
