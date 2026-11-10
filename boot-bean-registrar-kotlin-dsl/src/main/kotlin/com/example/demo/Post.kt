package com.example.demo

import java.util.UUID

data class Post(
    val id: UUID? = null,
    var title: String? = null,
    var content: String? = null
)