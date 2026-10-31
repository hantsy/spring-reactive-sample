package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@Configuration
@EnableR2dbcAuditing
class DataConfig {

//    @Bean
//    public ConnectionFactoryOptionsBuilderCustomizer postgresCustomizer() {
//        Map<String, String> options = new HashMap<>();
//        options.put("lock_timeout", "30s");
//        options.put("statement_timeout", "60s");
//        return (builder) -> builder.option(OPTIONS, options);
//    }

    @Bean
    ReactiveAuditorAware<String> auditorAware() {
        return () -> Mono.just("test");
    }
}


interface PostRepository extends R2dbcRepository<Post, UUID> {
}

@Table(value = "posts")
record Post(

        @Id
        @Column("id")
        UUID id,

        @Column("title")
        String title,

        @Column("content")
        String content,

        @Column("status")
        Status status,

        @Column("created_at")
        @CreatedDate
        LocalDateTime createdAt,

        @Column("created_by")
        @CreatedBy
        String createdBy,

        @Column("updated_at")
        @LastModifiedDate
        LocalDateTime updatedAt,

        @Column("updated_by")
        @LastModifiedBy
        String updatedBy,

        @Column("version")
        @Version
        Long version
) {

    public static Post of(String title, String content) {
        return new Post(null, title, content, null, null, null, null, null, null);
    }

    enum Status {
        DRAFT, PENDING_MODERATION, PUBLISHED;
    }

}
