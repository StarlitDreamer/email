package com.java.email.service;

import com.java.email.pojo.Email;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.UndeliveredEmail;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EmailService {


     void saveEmailTask(UndeliveredEmail emailTask) throws IOException;

     List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size,
                                                    Integer userRole, String userEmail, List<String> managedUserEmails) throws IOException;

     Email findById(String id) throws IOException;



}
