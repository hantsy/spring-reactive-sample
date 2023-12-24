package com.example.demo

import com.example.demo.domain.Post
import com.example.demo.repository.PostRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DataInitializer(val posts: PostRepository) {
    companion object {
        private val log = LoggerFactory.getLogger(DataInitializer::class.java)
    }

    @EventListener(value = [ContextRefreshedEvent::class])
    suspend fun run() {
        log.info("start data initialization ...")

        val samplePosts = listOf("Post one", "Post two").map { Post(title = it, content = "content of $it") }
        posts.saveAll(samplePosts).collect { log.debug("saved post: $it") }

        log.info("data initialization is done ...")
    }
}