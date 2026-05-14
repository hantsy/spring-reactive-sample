package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestSenderApplicationConfig {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return  new KafkaContainer(DockerImageName.parse("apache/kafka-native").withTag("4.2.0"));
    }

}
