package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("posts")
public record Post(@Id UUID id, String title, String content, Status status, LocalDateTime createdAt) {

    public static Post of(String title, String content) {
        return new Post(null, title, content, Status.DRAFT, LocalDateTime.now());
    }
}
