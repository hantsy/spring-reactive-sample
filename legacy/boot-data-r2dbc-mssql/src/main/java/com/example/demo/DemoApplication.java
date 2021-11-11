package com.example.demo;

import io.r2dbc.spi.Option;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public ConnectionFactoryOptionsBuilderCustomizer customizer() {
        Map<String, String> options = new HashMap<>();
        options.put("connectionTimeout", "60s");

        return (builder) -> options.keySet()
                .forEach(
                        optKey -> builder.option(Option.valueOf(optKey), options.get(optKey))

                );
    }
}

