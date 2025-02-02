package org.easyarch.email.service;

import org.easyarch.email.pojo.Email;
import org.easyarch.email.pojo.User;

import java.io.IOException;

public interface UserService {

    void saveUser(User user) throws IOException;
    User findById(String id) throws IOException;
    User findByUserEmail(String email) throws IOException;

    String findUserEmailByUserName(String senderName) throws IOException;
}
