package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

    @Id
    @Column("id")
    private Integer id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;
}
