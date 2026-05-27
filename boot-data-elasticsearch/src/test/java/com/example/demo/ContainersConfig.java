package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.Map;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @ServiceConnection
    @Bean
    ElasticsearchContainer elasticsearchContainer() {
        return new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.4.1")
                .withEnv(Map.of("xpack.security.enabled", "false"));
    }
}
