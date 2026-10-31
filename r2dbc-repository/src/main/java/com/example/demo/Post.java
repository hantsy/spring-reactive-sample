package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

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

        @Column("tags")
        List<String> tags,

        @Column("version")
        @Version
        Long version
) {

    public static Post of(String title, String content) {
        return new Post(null, title, content, null, List.of("default"), null);
    }

    public static Post of(String title, String content, List<String> tags) {
        return new Post(null, title, content, null, tags, null);
    }

    enum Status {
        DRAFT, PENDING_MODERATION, PUBLISHED;
    }

}
