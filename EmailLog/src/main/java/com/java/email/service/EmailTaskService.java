package com.java.email.service;

import com.java.email.pojo.EmailTask;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EmailTaskService {
     void saveEmailTask(EmailTask emailTask) throws IOException;

     List<EmailTask> findByDynamicQueryEmailTask(Map<String, String> params, int page, int size, Integer userRole, String userEmail,List<String> managedUserEmails) throws IOException;

     EmailTask findById(String id) throws IOException;

     List<EmailTask> findAll() throws IOException;

     void deleteById(String id) throws IOException;


     List<EmailTask> findByEmailTasks(Map<String, String> params, Integer userRole, String userEmail,List<String> managedUserEmails) throws IOException;
}
