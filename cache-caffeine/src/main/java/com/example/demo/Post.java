package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("posts")
public record Post(@Id UUID id,
                   String title,
                   String content
) {

    public static Post of(String title, String content) {
        var data = new Post(null, title, content);
        return data;
    }

}
