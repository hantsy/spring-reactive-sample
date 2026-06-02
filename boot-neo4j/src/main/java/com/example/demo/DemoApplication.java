package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.neo4j.autoconfigure.ConfigBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ConfigBuilderCustomizer configBuilderCustomizer() {
        return config -> config.withConnectionTimeout(5_000, TimeUnit.MILLISECONDS)
                ;
    }

}
