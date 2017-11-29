package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class DataInitializer(val posts: PostRepository) {
    private val log = LoggerFactory.getLogger(DataInitializer::class.java);

    @EventListener(value = ContextRefreshedEvent::class)
    fun run() {
        log.info("start data initialization ...")
        this.posts
                .deleteAll()
                .thenMany(
                        Flux.just("Post one", "Post two")
                                .flatMap { this.posts.save(Post(title = it, content = "content of " + it)) }
                )
                .log()
                .subscribe(
                        null,
                        null,
                        { log.info("done initialization...") }
                )
    }
}