package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@SpringBootApplication
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
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

        @Column("version")
        @Version
        Long version
) {

    public static Post of(String title, String content) {
        return new Post(null, title, content, null, null);
    }

    enum Status {
        DRAFT, PENDING_MODERATION, PUBLISHED;
    }

}
