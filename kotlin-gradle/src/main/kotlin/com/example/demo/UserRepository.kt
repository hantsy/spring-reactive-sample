package com.example.demo

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class UserRepository(val template: ReactiveMongoTemplate) {

    fun save(user: User) = template.save(user)

    fun deleteAll() = template.remove(Query(), User::class.java)

    fun findByUsername(username: String) = template.findOne(Query().addCriteria(Criteria.where("username").`is`(username)), User::
    class.java)
}