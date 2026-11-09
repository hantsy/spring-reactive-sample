package com.example.demo;

import java.time.LocalDateTime;
import java.util.UUID;

public record Post(UUID id, String title, String content, Status status, LocalDateTime createdAt) {
    public static Post of(String title, String content) {
        return new Post(null, title, content, Status.DRAFT, LocalDateTime.now());
    }

    public static Post of(String title, String content, Status status) {
        return new Post(null, title, content, status, LocalDateTime.now());
    }
}