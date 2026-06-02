package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.elasticsearch.autoconfigure.Rest5ClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    Rest5ClientBuilderCustomizer rest5ClientBuilderCustomizer() {
        return builder -> builder
                .setConnectionConfigCallback(connection -> connection
                        .setConnectTimeout(5_000, TimeUnit.MILLISECONDS)
                        .build())
                .build();
    }
}
