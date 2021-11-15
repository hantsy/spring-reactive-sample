package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

@SpringBootApplication
@EnableReactiveMongoAuditing
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    @Profile("default")
    public ApplicationRunner dataInitializer(ReactiveMongoTemplate template) {
        return (args) -> {
            log.debug("running ApplicationRunner...");
            template.insert(Message.builder().body("Welcome!").build()).then().block();
            template.executeCommand("{\"convertToCapped\": \"messages\", size: 100000}")
                    .log()
                    .subscribe(r -> log.debug("executed command: {}", r));
        };
    }

}

record MessageRequest(String message) {
}

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
class MessageController {
    private final MessageRepository messages;

    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> all() {
        return this.messages.getMessagesBy();
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> send(@RequestBody MessageRequest request) {
        return this.messages.save(Message.builder().body(request.message()).build())
                .map(m -> ResponseEntity.created(URI.create("/messages/" + m.getId())).build());
    }

}


interface MessageRepository extends ReactiveMongoRepository<Message, String> {

    @Tailable
    Flux<Message> getMessagesBy();
}

@Document("messages")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Message {
    @Id
    private String id;
    private String body;

    @CreatedDate
    private LocalDateTime sentAt;
}