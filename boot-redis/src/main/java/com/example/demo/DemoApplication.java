package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public RouterFunction<ServerResponse> routes(FavoriteHandler favoriteHandler) {
        RouterFunction<ServerResponse> usersRoutes = route(GET("/{username}/favorites"), favoriteHandler::favoritedPosts);
        RouterFunction<ServerResponse> postsRoutes = route(GET("/{slug}/favorited"), favoriteHandler::favorited)
                .andRoute(GET("/{slug}/favorites"), favoriteHandler::all)
                .andRoute(POST("/{slug}/favorites"), favoriteHandler::favorite)
                .andRoute(DELETE("/{slug}/favorites"), favoriteHandler::unfavorite);

        return nest(path("/posts"), postsRoutes)
                .andNest(path("/users"), usersRoutes);
    }
}

