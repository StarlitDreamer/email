package com.java.email.service.impl;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.java.email.pojo.EmailManage;
import com.java.email.service.EmailManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmailManageServiceImpl implements EmailManageService {

    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "email";
    
    @Autowired
    public EmailManageServiceImpl(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }
    
    @Override
    public List<EmailManage> findByEmailTaskId(String emailTaskId) {
        try {
            // 构建查询
            SearchResponse<EmailManage> response = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                    .term(t -> t
                        .field("email_task_id")
                        .value(emailTaskId)
                    )
                )
                .size(1000),
                EmailManage.class
            );
            
            return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("查询邮件状态失败: emailTaskId={}, error={}", emailTaskId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    /**
     * 根据邮件任务ID查询最新的邮件状态
     */
    @Override
    public Long findLatestStatusByTaskId(String emailTaskId) {
        try {
            // 构建查询
            SearchResponse<EmailManage> response = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                    .term(t -> t
                        .field("email_task_id")
                        .value(emailTaskId)
                    )
                )
                .size(1),  // 只获取最新的一条记录
                EmailManage.class
            );

            // 获取最新状态
            if (response.hits().total().value() > 0) {
                EmailManage latestEmail = response.hits().hits().get(0).source();
                if (latestEmail != null) {
                    log.debug("Found latest status {} for emailTaskId {}", 
                        latestEmail.getEmailStatus(), emailTaskId);
                    return latestEmail.getEmailStatus();
                }
            }

            log.info("未找到邮件状态记录: emailTaskId={}", emailTaskId);
            return null;

        } catch (Exception e) {
            log.error("查询最新邮件状态失败: emailTaskId={}, error={}", 
                emailTaskId, e.getMessage(), e);
            return null;
        }
    }


} 