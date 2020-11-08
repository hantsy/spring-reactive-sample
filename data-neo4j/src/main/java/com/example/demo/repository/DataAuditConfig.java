package com.example.demo.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.neo4j.config.EnableReactiveNeo4jAuditing;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveNeo4jAuditing
public class DataAuditConfig {

    @Bean
    ReactiveAuditorAware<String> auditorAware() {
        return () -> Mono.just("hantsy");
    }
}
