package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@HttpExchange(url = "/posts")
public interface PostHttpService {
    @GetExchange(accept = MediaType.APPLICATION_JSON_VALUE)
    Flux<Post> allPosts();

    @GetExchange(value = "/{id}", accept = MediaType.APPLICATION_JSON_VALUE)
    Mono<Post> getById(@PathVariable("id") UUID id);

    @PostExchange(contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> save(@RequestBody Post post);

    @PutExchange(value = "/{id}", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> update(@PathVariable UUID id, @RequestBody Post post);

    @DeleteExchange(value = "/{id}")
    Mono<ResponseEntity<Void>> delete(@PathVariable UUID id);
}
