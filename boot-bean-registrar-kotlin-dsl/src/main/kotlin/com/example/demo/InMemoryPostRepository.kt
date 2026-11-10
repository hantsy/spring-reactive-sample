package com.example.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class InMemoryPostRepository : PostRepository {

    private val store: MutableMap<UUID, Post> = mutableMapOf()

    override fun findAll(): Flow<Post> {
        return flowOf(*store.values.toTypedArray())
    }

    override suspend fun findById(id: UUID): Post? {
        return store[id]
    }

    override suspend fun save(post: Post): UUID {
        val id = UUID.randomUUID()
        store[id] = Post(id, post.title, post.content)
        return id
    }

    override suspend fun deleteById(id: UUID): Long {
        store.remove(id)
        return 1L
    }

    override suspend fun update(id: UUID, post: Post): Long {
        return if (store.containsKey(id)) {
            store[id] = Post(id, post.title, post.content)
            1L
        } else {
            0L
        }
    }

    override suspend fun initializeSampleData() {
        listOf("Spring 7", "Bean Registrar")
            .forEach { save(Post(title = it, content = "Content of $it")) }
    }
}