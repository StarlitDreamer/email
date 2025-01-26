package com.java.email.repository;

import com.java.email.entity.MailServerConfig;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface MailServerConfigRepository extends ElasticsearchRepository<MailServerConfig, String> {

    List<MailServerConfig> findByName(String name);
}
