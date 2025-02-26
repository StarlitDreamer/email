package com.java.email.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class ElasticsearchConfig {

    @Bean
    @Primary
    public ElasticsearchClient elasticsearchClient() {
        // 创建 RestClient
        RestClient restClient = RestClient.builder(
                new HttpHost("112.35.176.43", 9201)
        ).setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(100000)
                        .setSocketTimeout(600000)
        ).build();

        // 配置 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // 创建 Transport
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(objectMapper)  // 使用配置好的 ObjectMapper
        );

        return new ElasticsearchClient(transport);
    }
}
