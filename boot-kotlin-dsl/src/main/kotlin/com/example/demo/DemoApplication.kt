package com.example.demo

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDateTime

@SpringBootApplication
class DemoApplication


fun beans() = beans {
    bean {
        CommandLineRunner {
            println("start data initialization...")
            val posts = ref<PostRepository>()
// see: https://stackoverflow.com/questions/53743766/the-difference-between-concat-and-thenmany-in-reactor
//            Flux.concat(
//                    posts.deleteAll(),
//                    posts.saveAll(
//                            arrayListOf(
//                                    Post(null, "my first post", "content of my first post"),
//                                    Post(null, "my second post", "content of my second post")
//                            )
//                    )
//            )
            posts.deleteAll()
                .thenMany<Post>(
                    posts.saveAll(
                        arrayListOf(
                            Post(null, "my first post", "content of my first post"),
                            Post(null, "my second post", "content of my second post")
                        )
                    )
                )
                .log()
                .subscribe { println("data initialization done.") }
        }
    }

    bean {
        PostRoutes(PostHandler(ref())).routes()
    }

    bean {

        val config = CorsConfiguration().apply {
            // allowedOrigins = listOf("http://allowed-origin.com")
            // maxAge = 8000L
            // addAllowedMethod("PUT")
            // addAllowedHeader("X-Allowed")
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }

        CorsWebFilter(source)
    }

}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args) {
        addInitializers(beans())
    }
}

class PostRoutes(private val postHandler: PostHandler) {
    fun routes() = router {
        "/posts".nest {
            GET("", postHandler::all)
            GET("/{id}", postHandler::get)
            POST("", postHandler::create)
            PUT("/{id}", postHandler::update)
            DELETE("/{id}", postHandler::delete)
        }

    }
}

class PostHandler(private val posts: PostRepository) {

    fun all(req: ServerRequest): Mono<ServerResponse> {
        return ok().body(this.posts.findAll(), Post::class.java)
    }

    fun create(req: ServerRequest): Mono<ServerResponse> {
        return req.bodyToMono(Post::class.java)
            .flatMap { this.posts.save(it) }
            .flatMap { created(URI.create("/posts/" + it.id)).build() }
    }

    fun get(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
            .flatMap { ok().body(Mono.just(it), Post::class.java) }
            .switchIfEmpty(notFound().build())
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
            .zipWith(req.bodyToMono(Post::class.java))
            .map { it.t1.copy(title = it.t2.title, content = it.t2.content) }
            .flatMap { this.posts.save(it) }
            .flatMap { noContent().build() }
    }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.deleteById(req.pathVariable("id"))
            .flatMap { noContent().build() }
    }
}

@Document
data class Post(
    @Id var id: String? = null,
    var title: String? = null,
    var content: String? = null,
    @CreatedDate var createdDate: LocalDateTime = LocalDateTime.now()
)

interface PostRepository : ReactiveMongoRepository<Post, String>

