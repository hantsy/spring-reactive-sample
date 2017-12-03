package com.example.demo;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.BodyInserters;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route(GET("/"), 
            (ServerRequest req)-> ok()
                .body(
                    BodyInserters.fromObject(
                        Arrays.asList(
                            Message.builder().body("hello Spring 5").build(),
                            Message.builder().body("hello Spring Boot 2").build()
                        )
                    )
                )
        );
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Message {

    String body;
}
