package com.example.demo.repository

import com.example.demo.domain.Post
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepository : CoroutineCrudRepository<Post, String>