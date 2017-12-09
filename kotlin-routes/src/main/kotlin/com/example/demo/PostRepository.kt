package com.example.demo

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface PostRepository : ReactiveMongoRepository<Post, String>