package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.Map;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @ServiceConnection
    @Bean
    ElasticsearchContainer elasticsearchContainer() {
        return new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:latest")
                .withEnv(Map.of("xpack.security.enabled", "false"));
    }
}
