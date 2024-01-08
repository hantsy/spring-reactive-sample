package com.example.demo

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange
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
            runBlocking {
                posts.deleteAll()
                posts
                    .saveAll(
                        arrayListOf(
                            Post(null, "my first post", "content of my first post"),
                            Post(null, "my second post", "content of my second post")
                        )
                    )
                    .collect { println("saved post: $it") }
            }
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
    fun routes() = coRouter {
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

    suspend fun all(req: ServerRequest): ServerResponse {
        val data = this.posts.findAll()
        return ok().bodyAndAwait(data)
    }

    suspend fun create(req: ServerRequest): ServerResponse {
        val formData = req.awaitBody<Post>()
        val saved = this.posts.save(formData)
        return created(URI.create("/posts/" + saved.id)).buildAndAwait()
    }

    suspend fun get(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id")
        val post = this.posts.findById(id) ?: throw PostNotFoundException(id)
        return ok().bodyValueAndAwait(post)
    }

    suspend fun update(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id")
        val existed = this.posts.findById(id) ?: throw PostNotFoundException(id)
        val formData = req.awaitBody<Post>()

        existed.apply {
            title = formData.title
            content = formData.content
        }

        this.posts.save(existed)
        return noContent().buildAndAwait()
    }

    suspend fun delete(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id")
        this.posts.deleteById(id)
        return noContent().buildAndAwait()
    }
}

@Component
class ExceptionFilter : CoWebFilter() {
    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        try {
            chain.filter(exchange)
        } catch (e: PostNotFoundException) {
            exchange.response.statusCode = HttpStatus.NOT_FOUND
            exchange.response.setComplete().awaitSingleOrNull()
        }
    }
}

class PostNotFoundException(id: String) : RuntimeException("Post $id was not found")

@Document
data class Post(
    @Id var id: String? = null,
    var title: String? = null,
    var content: String? = null,
    @CreatedDate var createdDate: LocalDateTime = LocalDateTime.now()
)

interface PostRepository : CoroutineCrudRepository<Post, String>,
    CoroutineSortingRepository<Post, String>

