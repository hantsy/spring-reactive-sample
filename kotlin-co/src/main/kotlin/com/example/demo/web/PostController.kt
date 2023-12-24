package com.example.demo.web

import com.example.demo.domain.Post
import com.example.demo.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

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
    suspend fun delete(@PathVariable("id") id: String): ResponseEntity<Void>{
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