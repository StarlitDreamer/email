package com.java.email.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.vo.EmailTaskVo;
import com.java.email.vo.EmailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class EmailLogService {

    private final ElasticsearchClient esClient;

    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailTaskService emailTaskService;
    @Autowired
    private EmailTypeService emailTypeService;



    public EmailLogService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }


    public EmailTask saveEmailTask(EmailTask emailTask) throws IOException {
        emailTaskService.saveEmailTask(emailTask);
        return emailTask;
    }



    public EmailTaskVo findByDynamicQueryEmailTask(Map<String, String> params, int page, int size, Integer userRole, String userEmail, List<String> managedUserEmails) throws IOException {
        return emailTaskService.findByDynamicQueryEmailTask(params, page, size, userRole, userEmail, managedUserEmails);
    }

    public EmailVo findByDynamicQueryEmail(Map<String, String> params, int page, int size, Integer userRole, String userEmail, List<String> managedUserEmails) throws IOException {

        return emailService.findByDynamicQueryEmail(params, page, size, userRole, userEmail, managedUserEmails);
    }



    public EmailTask findByEmailTasks(Map<String, String> params,Integer userRole,String userEmail ,List<String> managedUserEmails) throws IOException {
        return emailTaskService.findByEmailTasks(params, userRole, userEmail,managedUserEmails);
    }

    public String findByEmailTypeName(String emailTypeId) throws IOException {
        return emailTypeService.findByEmailTypeName(emailTypeId);
    }

    public EmailVo findByDynamicQueryUndeliveredEmail(Map<String, String> params, int page, int size, Integer userRole, String userEmail, List<String> finalManagedUserEmails) throws IOException {
        return emailService.findByDynamicQueryUndeliveredEmail(params, page, size, userRole, userEmail, finalManagedUserEmails);
    }
}