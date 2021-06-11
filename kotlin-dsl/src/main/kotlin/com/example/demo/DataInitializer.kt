package com.example.demo

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux


class DataInitializer(private val postRepository: PostRepository) {
    private val log = LoggerFactory.getLogger(DataInitializer::class.java)

    // @EventListener(ContextRefreshedEvent::class)
    // @PostConstruct
    fun initData() {
        log.info("start data initialization ...")
        this.postRepository
                .deleteAll()
                .thenMany(
                        Flux
                                .just("Post one", "Post two")
                                .flatMap { it -> this.postRepository.save(Post(title = it, content = "content of " + it)) }
                )
                .log()
                .subscribe(
                        null,
                        null
                ) { log.info("done post initialization...") }

    }
}