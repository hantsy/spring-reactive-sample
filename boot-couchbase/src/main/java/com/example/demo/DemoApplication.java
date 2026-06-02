package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.couchbase.autoconfigure.ClusterEnvironmentBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ClusterEnvironmentBuilderCustomizer clusterEnvironmentBuilderCustomizer() {
        return builder -> builder
                .timeoutConfig()
                .connectTimeout(Duration.ofSeconds(120))
                .queryTimeout(Duration.ofSeconds(120))
                .build();
    }

}
