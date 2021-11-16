package com.example.demo;


import static org.springframework.http.ResponseEntity.created;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<Message> messageStream() {
    return this.messageService.latestMessages();
  }

  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<?>> send(@RequestBody MessageRequest request) {
    var message = Message.builder().id(UUID.randomUUID()).body(request.message()).sentAt(
        LocalDateTime.now()).build();
    this.messageService.send(message);
    return Mono.just(created(URI.create("/messages/" + message.getId())).build());
  }
}
