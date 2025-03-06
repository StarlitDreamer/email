package com.java.email.service;

import com.java.email.pojo.Email;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.vo.EmailVo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EmailService {


     void saveEmailTask(UndeliveredEmail emailTask) throws IOException;

     EmailVo findByDynamicQueryEmail(Map<String, String> params, int page, int size,

                                     Integer userRole, String userEmail, List<String> managedUserEmails) throws IOException;



     Email findById(String id) throws IOException;


     EmailVo findByDynamicQueryUndeliveredEmail(Map<String, String> params, List<String> emailTaskIds,int page, int size, Integer userRole, String userEmail, List<String> finalManagedUserEmails) throws IOException;
}
