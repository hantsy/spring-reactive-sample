package com.example.demo;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.internal.core.type.codec.registry.DefaultCodecRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.cassandra.autoconfigure.CqlSessionBuilderCustomizer;
import org.springframework.boot.cassandra.autoconfigure.DriverConfigLoaderBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


    @Bean
    CqlSessionBuilderCustomizer  cqlSessionBuilderCustomizer() {
        return builder -> builder
                //.withCodecRegistry(DefaultCodecRegistry.DEFAULT)
                .build();
    }

    @Bean
    DriverConfigLoaderBuilderCustomizer driverConfigLoaderBuilderCustomizer() {
        return builder -> builder
                .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofMillis(15_000));
    }

}
