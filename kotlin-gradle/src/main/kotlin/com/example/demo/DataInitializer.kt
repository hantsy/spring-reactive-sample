package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class DataInitializr(val posts: PostRepository) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(DataInitializr::class.java);

    override fun run(vararg strings: String) {
        log.info("start data initialization ...")
        this.posts
                .deleteAll()
                .thenMany(
                        Flux
                                .just("Post one", "Post two")
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