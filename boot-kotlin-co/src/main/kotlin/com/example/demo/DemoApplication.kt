package com.example.demo

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import java.net.URI
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
    fun all(): ResponseEntity<Flow<Post>> = ResponseEntity.ok(this.posts.findAll())

    @PostMapping("")
    suspend fun create(@RequestBody post: Post): ResponseEntity<Void> {
        val saved = this.posts.save(post)
        return ResponseEntity.created(URI.create("/posts/${saved.id}")).build()
    }

    @GetMapping("/{id}")
    suspend fun get(@PathVariable("id") id: String): ResponseEntity<Post> {
        val existed = this.posts.findById(id) ?: throw PostNotFoundException(id)
        return ResponseEntity.ok(existed)
    }

    @PutMapping("/{id}")
    suspend fun update(@PathVariable("id") id: String, @RequestBody post: Post): ResponseEntity<Void> {
        val existed = this.posts.findById(id) ?: throw PostNotFoundException(id)
        existed.apply {
            title = post.title
            content = post.content
        }

        this.posts.save(existed)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable("id") id: String): ResponseEntity<Void> {
        this.posts.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(value = [PostNotFoundException::class])
    suspend fun handlePostNotFoundException(e: PostNotFoundException): ProblemDetail {
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: "Post was not found")
        detail.apply {
            title = "Not Found"
            type = URI.create("http://example.com/not_found")
        }
        return detail
    }

}

class PostNotFoundException(id: String) : RuntimeException("Post $id was not found")

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

interface PostRepository : CoroutineCrudRepository<Post, String>

@Document
data class Post(
    @Id var id: String? = null,
    var title: String? = null,
    var content: String? = null,
    var createdDate: LocalDateTime = LocalDateTime.now()
)