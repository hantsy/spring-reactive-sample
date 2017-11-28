package com.example.demo

import groovy.transform.builder.Builder
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import java.time.LocalDateTime

@Document
@Builder
class Post {

    @Id
    String id
    String title
    String content

    @CreatedDate
    LocalDateTime createdDate
}