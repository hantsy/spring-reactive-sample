package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.repository.auditing.EnableReactiveCouchbaseAuditing;
import org.springframework.data.domain.ReactiveAuditorAware;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
// see: https://jira.spring.io/browse/DATACOUCH-644
@EnableReactiveCouchbaseAuditing
public class DataConfig {

    @Bean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {
        return () -> Mono.just("hantsy");
    }
}
