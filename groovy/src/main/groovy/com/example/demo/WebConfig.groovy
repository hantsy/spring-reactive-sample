package com.example.demo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

import static org.springframework.web.reactive.function.server.RequestPredicates.*
import static org.springframework.web.reactive.function.server.RouterFunctions.route

@Configuration
@EnableWebFlux
class WebConfig {

    @Bean
    RouterFunction<ServerResponse> routes(PostHandler postHandler) {
        route(GET("/posts"), postHandler.&all)
                .andRoute(POST("/posts"), postHandler.&create)
                .andRoute(GET("/posts/{id}"), postHandler.&get)
                .andRoute(PUT("/posts/{id}"), postHandler.&update)
                .andRoute(DELETE("/posts/{id}"), postHandler.&delete)
    }

}
