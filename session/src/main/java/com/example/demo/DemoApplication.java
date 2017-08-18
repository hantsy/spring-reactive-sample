package com.example.demo;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
@EnableMongoAuditing
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

//    @Bean
//    HttpSessionStrategy httpSessionStrategy() {
//        return new HeaderHttpSessionStrategy();
//    }
    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postController, UserHandler userHandler) {
          RouterFunction<ServerResponse> postsRoutes = route(GET("/posts"), postController::all)
            .andRoute(POST("/posts"), postController::create)
            .andRoute(GET("/posts/{id}"), postController::get)
            .andRoute(PUT("/posts/{id}"), postController::update)
            .andRoute(DELETE("/posts/{id}"), postController::delete);
          
          RouterFunction<ServerResponse> userRoutes = route(GET("/user"), userHandler::current);
          
          return nest(path("/posts"), postsRoutes)
              .andNest(path("/user"), userRoutes);
    }
    
    @Bean
    public AuditorAware<Username> auditorAware() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("current authentication:" + authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            return () -> Optional.<Username>empty();
        }

        return () -> Optional.of(
            Username.builder()
                .username(((UserDetails) authentication.getPrincipal()).getUsername())
                .build()
        );
    }
}
