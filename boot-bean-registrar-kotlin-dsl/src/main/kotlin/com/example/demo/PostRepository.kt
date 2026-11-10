package com.example.demo

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface PostRepository {
    suspend fun findById(id: UUID): Post?
    fun findAll(): Flow<Post>
    suspend fun save(post: Post): UUID
    suspend fun update(id: UUID, post: Post): Long
    suspend fun deleteById(id: UUID): Long
    suspend fun initializeSampleData()
}