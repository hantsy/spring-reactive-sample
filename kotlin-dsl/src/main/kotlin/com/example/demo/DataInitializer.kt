package com.example.demo

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux


class DataInitializr(private val posts: PostRepository) {
    private val log = LoggerFactory.getLogger(DataInitializr::class.java);

    // @EventListener(ContextRefreshedEvent::class)
    // @PostConstruct
    fun initData() {
        log.info("start data initialization ...")
        this.posts
            .deleteAll()
            .thenMany(
                Flux
                    .just("Post one", "Post two")
                    .flatMap { it -> this.posts.save(Post(title = it, content = "content of $it")) }
            )
            .log()
            .subscribe { log.info("done post initialization...") }
    }
}