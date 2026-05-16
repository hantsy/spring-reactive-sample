package com.example.demo;

import org.springframework.boot.SpringApplication;

public class TestDemoApplication {
    public static void main(String[] args) {
        SpringApplication.from(DemoApplication::main)
                .withAdditionalProfiles("dev")
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
