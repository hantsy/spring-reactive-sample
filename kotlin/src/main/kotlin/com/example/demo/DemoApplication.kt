package com.example.demo

import org.slf4j.LoggerFactory.*
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.HttpMethod
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.MapUserDetailsRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@SpringBootApplication
@EnableMongoAuditing
class DemoApplication

fun main(args: Array<String>) {
    SpringApplication.run(DemoApplication::class.java, *args)
}


@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun springWebFilterChain(http: HttpSecurity): SecurityWebFilterChain {
        return http
                .authorizeExchange()
                .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                .anyExchange().authenticated()
                .and()
                .build()
    }
    private fun currentUserMatchesPath(authentication:Mono<Authentication>, context: AuthorizationContext):Mono<AuthorizationDecision> {
        return authentication
                .map({ a-> context.variables?.get("user")?.equals(a.name) })
                .map({ granted-> AuthorizationDecision(granted?:false) })
    }
    @Bean
    fun userDetailsRepository(): MapUserDetailsRepository {
        val rob = User.withUsername("test").password("test123").roles("USER").build()
        val admin = User.withUsername("admin").password("admin123").roles("USER", "ADMIN").build()
        return MapUserDetailsRepository(rob, admin)
    }
}

@RestController
@RequestMapping(value = "/posts")
class PostController(val posts:PostRepository) {

    @GetMapping("")
    fun all():Flux<Post> = this.posts.findAll()

    @PostMapping("")
    fun create(@RequestBody post:Post): Mono<Post> = this.posts.save(post)

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id:String):Mono<Post> = this.posts.findById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable("id") id:String, @RequestBody post:Post):Mono<Post> {
        return this.posts.findById(id)
                .map({
                    p->p.copy(title = post.title, content = post.content)
                })
                .flatMap({ p-> this.posts.save(p) })
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id:String):Mono<Void> = this.posts.deleteById(id)

}

@Component
class DataInitializr(val posts: PostRepository) : CommandLineRunner {
    private val log = getLogger(DataInitializr::class.java);

    override fun run(vararg strings: String) {
        log.info("start data initialization ...")
         this.posts
                .deleteAll()
                .thenMany(
                    Flux
                        .just("Post one", "Post two")
                        .flatMap({ title -> this.posts.save(Post(title = title, content = "content of " + title)) })
                )
                .log()
                .subscribe(
                    null,
                    null,
                    { log.info("done initialization...") }
                )
    }
}


interface PostRepository: ReactiveMongoRepository<Post, String>

@Document
data class Post(@Id var id: String? = null,
                var title: String? = null,
                var content: String?= null,
                @CreatedDate var createdDate: LocalDateTime = LocalDateTime.now()
)