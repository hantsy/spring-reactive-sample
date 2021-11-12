package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    SpringApplication.run(DemoApplication::class.java, *args)
}

@RestController
@RequestMapping(value = ["/posts"])
class PostController(val posts: PostRepository) {

    @GetMapping("")
    fun all(): Flux<Post> = this.posts.findAll()

    @PostMapping("")
    fun create(@RequestBody post: Post): Mono<Post> = this.posts.save(post)

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: String): Mono<Post> = this.posts.findById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable("id") id: String, @RequestBody post: Post): Mono<Post> {
        return this.posts.findById(id)
            .map { it.copy(title = post.title, content = post.content) }
            .flatMap { this.posts.save(it) }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: String): Mono<Void> = this.posts.deleteById(id)

}

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
                    .flatMap { this.posts.save(Post(title = it, content = "content of $it")) }
            )
            .log()
            .subscribe { log.info("done initialization...") }
    }
}


interface PostRepository : ReactiveMongoRepository<Post, String>

@Document
data class Post(
    @Id var id: String? = null,
    var title: String? = null,
    var content: String? = null,
    var createdDate: LocalDateTime = LocalDateTime.now()
)