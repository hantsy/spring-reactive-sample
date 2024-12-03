package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class WebConfig {
    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler handler) {
        return route(GET("/posts"), handler::all)
                .andRoute(POST("/posts"), handler::create)
                .andRoute(GET("/posts/{id}"), handler::get)
                .andRoute(PUT("/posts/{id}"), handler::update)
                .andRoute(DELETE("/posts/{id}"), handler::delete);
    }
}
