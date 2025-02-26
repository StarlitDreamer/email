package com.java.email.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.java.email.pojo.EmailReport;
import com.java.email.service.EmailReportService;

import java.io.IOException;

public class EmailReportServiceImpl implements EmailReportService {

    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "email_report";

    public EmailReportServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public EmailReport getEmailReport(String emailTaskId) throws IOException {
        SearchResponse<EmailReport> response=esClient.search(s->{
            s.index(INDEX_NAME);
            if(emailTaskId!=null){
                s.query(q->q.term(m->m.field("email_task_id").value(emailTaskId)));
            }

            return s;
        },EmailReport.class);
        return response.hits().hits().get(0).source();
    }
}
