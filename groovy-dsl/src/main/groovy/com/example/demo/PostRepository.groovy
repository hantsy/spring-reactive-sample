package com.example.demo

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria

class PostRepository {
    ReactiveMongoTemplate template

    def findAll() { template.findAll(Post.class) }

    def findById(id) {
        template.findById(id, Post.class)
    }

    def save(post) {
        template.save(post)
    }

    def deleteAll() {
        template.remove(Query(), Post.class)
    }

    def deleteById(id) {
        template.remove(Query().addCriteria(Criteria.where("id").is(id)), Post.class)
    }

}

