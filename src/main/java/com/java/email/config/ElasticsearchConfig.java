package com.java.email.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.java.email.esdao.repository")
public class ElasticsearchConfig {
    
    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;
    
    @Value("${spring.elasticsearch.username}")
    private String username;
    
    @Value("${spring.elasticsearch.password}")
    private String password;
    
    @Bean
    public RestClient restClient() {
        String host = elasticsearchUrl.replace("http://", "").split(":")[0];
        int port = Integer.parseInt(elasticsearchUrl.replace("http://", "").split(":")[1]);
        
        // 创建认证提供者
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
        );
        
        // 构建 RestClient，添加认证信息
        return RestClient.builder(new HttpHost(host, port))
            .setHttpClientConfigCallback(httpClientBuilder -> 
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
            .build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}