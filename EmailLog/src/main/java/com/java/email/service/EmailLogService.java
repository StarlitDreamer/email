package com.java.email.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.UndeliveredEmail;
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
    private UndeliveredEmailService undeliveredEmailService;



    public EmailLogService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }


    public EmailTask saveEmailTask(EmailTask emailTask) throws IOException {
        emailTaskService.saveEmailTask(emailTask);
        return emailTask;
    }



    public List<EmailTask> findByDynamicQueryEmailTask(Map<String, String> params, int page, int size,Integer userRole,String userEmail,List<String> managedUserEmails) throws IOException {
        return emailTaskService.findByDynamicQueryEmailTask(params, page, size, userRole, userEmail, managedUserEmails);
    }

    public List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size,Integer userRole,String userEmail,List<String> managedUserEmails) throws IOException {

        return emailService.findByDynamicQueryEmail(params, page, size, userRole, userEmail, managedUserEmails);
    }



    public List<EmailTask> findByEmailTasks(Map<String, String> params,Integer userRole,String userEmail ,List<String> managedUserEmails) throws IOException {
        return emailTaskService.findByEmailTasks(params, userRole, userEmail,managedUserEmails);
    }
}