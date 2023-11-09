package com.example.demo;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


@RequiredArgsConstructor
public class PostClient {

    private final WebClient client;

    Flux<Post> allPosts() {
        return client.get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(Post.class));

    }

    Mono<Post> getById(UUID id) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/posts/{id}").build(id))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(Post.class);
                    }

                    return response.createError();
                });
    }

    Mono<ResponseEntity<Void>> save(Post post) {
        return client.post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.CREATED)) {
                        return response.toBodilessEntity();
                    }

                    return response.createError();
                });
    }

    Mono<ResponseEntity<Void>> update(UUID id, Post post) {
        return client.put()
                .uri(uriBuilder -> uriBuilder.path("/posts/{id}").build(id))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        return response.toBodilessEntity();
                    }

                    return response.createError();
                });
    }

    Mono<ResponseEntity<Void>> delete(UUID id) {
        return client.delete()
                .uri(uriBuilder -> uriBuilder.path("/posts/{id}").build(id))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        return response.toBodilessEntity();
                    }

                    return response.createError();
                });
    }

}
