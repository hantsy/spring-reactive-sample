package com.example.demo

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

class PostRepository(private val template: ReactiveMongoTemplate) {
    fun findAll() = template.findAll(Post::class.java)
    fun findById(id: String) = template.findById(id, Post::class.java)
    fun save(post: Post) = template.save(post)
    fun deleteAll() = template.remove(Query(), Post::class.java)
    fun deleteById(id: String) = template.remove(Query().addCriteria(Criteria.where("id").`is`(id)), Post::class.java)
}