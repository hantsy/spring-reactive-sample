package com.example.demo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates.*
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.RouterFunction


@Configuration
@EnableWebFlux
class WebConfig {

    @Bean
    fun routes(handler: PostHandler): RouterFunction<ServerResponse> {
        return route(GET("/posts"), HandlerFunction<ServerResponse>(handler::all))
                .andRoute(POST("/posts"), HandlerFunction<ServerResponse>(handler::create))
                .andRoute(GET("/posts/{id}"), HandlerFunction<ServerResponse>(handler::get))
                .andRoute(PUT("/posts/{id}"), HandlerFunction<ServerResponse>(handler::update))
                .andRoute(DELETE("/posts/{id}"), HandlerFunction<ServerResponse>(handler::delete))
    }
}