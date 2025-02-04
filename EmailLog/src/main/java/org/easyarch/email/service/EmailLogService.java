package org.easyarch.email.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.easyarch.email.pojo.Email;
import org.easyarch.email.pojo.EmailTask;
import org.easyarch.email.pojo.UndeliveredEmail;
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

    public UndeliveredEmail saveUndeliveredEmail(UndeliveredEmail undeliveredEmail) throws IOException {
        undeliveredEmailService.saveEmail(undeliveredEmail);
        return undeliveredEmail;
    }

    public EmailTask findByEmailTaskId(String id) throws IOException {
        return emailTaskService.findById(id);
    }

    public List<UndeliveredEmail> findAllEmail(String emailTaskId) throws IOException {
        return emailService.findAllEmail(emailTaskId);
    }

    public void deleteByEmailId(String id) throws IOException {
        emailService.deleteById(id);
    }

    public List<EmailTask> findByDynamicQueryEmailTask(Map<String, String> params, int page, int size) throws IOException {
        return emailTaskService.findByDynamicQueryEmailTask(params, page, size);
    }

    public List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size) throws IOException {

        return emailService.findByDynamicQueryEmail(params, page, size);
    }

    public List<UndeliveredEmail> findByDynamicQueryFailEmail(Map<String, String> params, int page, int size) throws IOException {

        return undeliveredEmailService.findByDynamicQueryEmail(params, page, size);
    }


    public List<EmailTask> findByEmailTasks(Map<String, String> params) throws IOException {
        return emailTaskService.findByEmailTasks(params);
    }
}