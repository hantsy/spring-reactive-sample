package com.example.demo

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.net.URI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class DemoApplication {

    @Bean
    fun routes(postHandler: PostHandler, userInfoHandler: UserInfoHandler): RouterFunction<ServerResponse> = router {
        "/posts".nest {
            GET("", postHandler::all)
            GET("/{id}", postHandler::get)
            POST("", postHandler::create)
            PUT("/{id}", postHandler::update)
            DELETE("/{id}", postHandler::delete)
        }
        "/auth".nest {
            GET("/user", userInfoHandler::userInfo)
        }
    }

    @Bean
    fun initData(posts: PostRepository) = CommandLineRunner {
        listOf(
            Post(title = "Learn Spring KoFu", content = "content of my first post"),
            Post(title = "Learn Spring and Kotlin", content = "content of my second post")
        ).forEach { posts.save(it).block() }
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Component
class UserInfoHandler {
    fun userInfo(req: ServerRequest): Mono<ServerResponse> {
        return req.principal()
            .map { user ->
                mapOf<String, Any>(
                    "user" to user.name,
                    "roles" to (user as Authentication).authorities.map { it.authority }
                )
            }
            .flatMap { ok().bodyValue(it) }
    }
}

@Component
class PostHandler(private val posts: PostRepository) {

    fun all(req: ServerRequest): Mono<ServerResponse> {
        return ok().body(this.posts.findAll(), Post::class.java)
    }

    fun get(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
            .flatMap { ok().bodyValue(it) }
            .switchIfEmpty { notFound().build() }
    }

    fun create(req: ServerRequest): Mono<ServerResponse> {
        return req.bodyToMono(Post::class.java)
            .flatMap { this.posts.save(it) }
            .flatMap { created(URI.create("/posts/${it.id}")).build() }
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
            .zipWith(req.bodyToMono(Post::class.java))
            .map { it.t1.copy(title = it.t2.title, content = it.t2.content) }
            .flatMap { this.posts.save(it) }
            .flatMap { noContent().build() }
            .switchIfEmpty { notFound().build() }
    }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.deleteById(req.pathVariable("id"))
            .then(noContent().build())
    }
}

@Component
class PostRepository {

    private val posts = ConcurrentHashMap<String, Post>()

    fun findAll(): Flux<Post> = Flux.fromIterable(posts.values)

    fun findById(id: String): Mono<Post> = Mono.justOrEmpty(posts[id])

    fun save(post: Post): Mono<Post> {
        val id = post.id ?: UUID.randomUUID().toString()
        val saved = post.copy(id = id)
        posts[id] = saved
        return Mono.just(saved)
    }

    fun deleteById(id: String): Mono<Void> {
        posts.remove(id)
        return Mono.empty()
    }
}

data class Post(
    var id: String? = null,
    var title: String? = null,
    var content: String? = null
)