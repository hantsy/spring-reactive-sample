package com.example.demo

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping(value = "/posts")
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