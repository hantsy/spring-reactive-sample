package com.example.demo

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class Post(
        @Id var id: String? = null,
        var title: String? = null,
        var content: String? = null,
        @CreatedDate var createdDate: LocalDateTime = LocalDateTime.now()
)