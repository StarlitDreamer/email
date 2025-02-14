package com.java.email.service;

import com.java.email.pojo.Email;
import com.java.email.pojo.UndeliveredEmail;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EmailService {


     List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size) throws IOException;

     Email findById(String id) throws IOException;

     List<UndeliveredEmail> findAllEmail(String emaIlTaskId) throws IOException;

     List<Email> findAll() throws IOException;

     void deleteById(String id) throws IOException;
}
