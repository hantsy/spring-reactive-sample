package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
@RequestMapping
class MessageController {

    @GetMapping
    Flux<Message> allMessages(){
        return Flux.just(
            Message.builder().body("hello Spring 5").build(),
            Message.builder().body("hello Spring Boot 2").build()
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
