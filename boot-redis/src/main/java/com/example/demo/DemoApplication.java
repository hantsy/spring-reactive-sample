package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@Configuration
class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Post> reactiveJsonPostRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        RedisSerializationContext<String, Post> serializationContext = RedisSerializationContext
                .<String, Post>newSerializationContext(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .hashValue(new Jackson2JsonRedisSerializer<>(Post.class))
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}

@Configuration
class WebConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(FavoriteHandler favoriteHandler, PostHandler postHandler) {
        RouterFunction<ServerResponse> usersRoutes = route(GET("/{username}/favorites"), favoriteHandler::favoritedPosts);
        RouterFunction<ServerResponse> postsRoutes = route(GET("/"), postHandler::all)
                .andRoute(POST("/"), postHandler::create)
                .andRoute(GET("/{id}"), postHandler::get)
                .andRoute(PUT("/{id}"), postHandler::update)
                .andRoute(DELETE("/{id}"), postHandler::delete)
                .andRoute(GET("/{slug}/favorited"), favoriteHandler::favorited)
                .andRoute(GET("/{slug}/favorites"), favoriteHandler::all)
                .andRoute(POST("/{slug}/favorites"), favoriteHandler::favorite)
                .andRoute(DELETE("/{slug}/favorites"), favoriteHandler::unfavorite);

        return nest(path("/posts"), postsRoutes)
                .andNest(path("/users"), usersRoutes);
    }
}

@Configuration
class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(it -> it.securityContextRepository(NoOpServerSecurityContextRepository.getInstance()))
                .authorizeExchange(it -> it.pathMatchers("/posts/*/favorites", "/posts/*/favorited").authenticated()
                        .pathMatchers(HttpMethod.POST, "/posts/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/posts/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/posts/**").authenticated()
                        .anyExchange().permitAll())
                .build();
    }
}


