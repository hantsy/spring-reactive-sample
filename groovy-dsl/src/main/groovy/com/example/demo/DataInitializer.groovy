package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

class DataInitializer  {
    def log = LoggerFactory.getLogger(DataInitializer)

    PostRepository posts

    DataInitializer(PostRepository posts){
        this.posts= posts
    }

    void run() {
        log.info("start data initialization  ...")
        this.posts
                .deleteAll()
                .thenMany(
                Flux.just("Post one", "Post two")
                        .flatMap { it -> this.posts.save(Post.builder().title(it).content("content of " + it).build()) }


        )
                .log()
                .subscribe(
                null,
                null,
                { log.info("done initialization...") }
        )

    }

}


