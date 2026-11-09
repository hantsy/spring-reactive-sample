package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class SampleDataInitializer {

    private final PostRepository posts;

    // Runs before ApplicationReadyEvent so the client demo finds the sample data.
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        Flux.just("First", "Second")
                .map(it -> new Post(null, it + " Post", "Content of " + it + " Post", Status.DRAFT, LocalDateTime.now()))
                .flatMap(posts::save)
                .subscribe(
                        data -> log.debug("saved post: {}", data),
                        error -> log.error("error: {}", error),
                        () -> log.info("saved post successfully")
                );
    }
}