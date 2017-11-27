package com.example.demo

import groovy.transform.Immutable
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.slf4j.LoggerFactory
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
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.time.LocalDateTime

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE
import static org.springframework.web.reactive.function.server.RequestPredicates.GET
import static org.springframework.web.reactive.function.server.RequestPredicates.POST
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT
import static org.springframework.web.reactive.function.server.RouterFunctions.route
import static reactor.core.publisher.Mono.zip

@SpringBootApplication
@EnableMongoAuditing
class DemoApplication {

    static void main(String[] args) {
        SpringApplication.run DemoApplication, args
    }

    @Bean
    RouterFunction<ServerResponse> routes(PostHandler postController) {
        return route(GET("/posts"), postController.&all)
        .andRoute(POST("/posts"), postController.&create)
        .andRoute(GET("/posts/{id}"), postController.&get)
        .andRoute(PUT("/posts/{id}"), postController.&update)
        .andRoute(DELETE("/posts/{id}"), postController.&delete)
    }

}

@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
        .authorizeExchange()
        .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
        .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
        //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
        .anyExchange().authenticated()
        .and()
        .build()
    }

    private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
        .map { (context.getVariables().get("user") == it.getName()) }
        .map { new AuthorizationDecision(it) }
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build()
        UserDetails admin = User.withDefaultPasswordEncoder().username("admin").password("password").roles("USER", "ADMIN").build()
        return new MapReactiveUserDetailsService(user, admin)
    }

}

@Component
class DataInitializer implements CommandLineRunner {
    def log = LoggerFactory.getLogger(DataInitializer)

    private final PostRepository posts

    DataInitializer(PostRepository posts) {
        this.posts = posts
    }

    @Override
    void run(String[] args) {
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

@Component
class PostHandler {

    private final PostRepository posts

    PostHandler(PostRepository posts) {
        this.posts = posts
    }

    Mono<ServerResponse> all(ServerRequest req) {
        return ServerResponse.ok().body(this.posts.findAll(), Post.class)
    }

    Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(Post.class)
        .flatMap { this.posts.save(it) }
        .flatMap { ServerResponse.created(URI.create("/posts/".concat(it.getId()))).build() }
    }

    Mono<ServerResponse> get(ServerRequest req) {
        return this.posts.findById(req.pathVariable("id"))
        .flatMap { ServerResponse.ok().body(Mono.just(it), Post.class) }
        .switchIfEmpty { ServerResponse.notFound().build() }
    }

    Mono<ServerResponse> update(ServerRequest req) {

        return Mono
        .zip(
            {
                Post p = (Post) it[0]
                Post p2 = (Post) it[1]
                p.title = p2.title
                p.content = p2.content
                p
            },
            this.posts.findById(req.pathVariable("id")),
            req.bodyToMono(Post.class)        
        )
        .cast(Post.class)
        .flatMap { this.posts.save(it) }
        .flatMap { ServerResponse.noContent().build() }

    }

    Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent().build(this.posts.deleteById(req.pathVariable("id")))
    }

}

interface PostRepository extends ReactiveMongoRepository<Post, String> {
}

@Document
@Builder
class Post {

    @Id
    String id
    String title
    String content

    @CreatedDate
    LocalDateTime createdDate
}