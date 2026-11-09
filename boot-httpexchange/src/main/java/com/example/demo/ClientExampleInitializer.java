package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@ConditionalOnProperty(name = "demo.client-runner.enabled", havingValue = "true")
public class ClientExampleInitializer {

    private final PostApi client;

    public ClientExampleInitializer(
            // the HTTP service client proxy registered by @ImportHttpServices(group = "post")
            @Qualifier("post#com.example.demo.PostApi") PostApi client) {
        this.client = client;
    }

    @EventListener(ApplicationReadyEvent.class)
    void postClientExample() {

        log.debug("get all posts.");
        client.allPosts()
                .subscribe(
                        data -> log.debug("The existing post: {}", data)
                );

        log.debug("save post and update post");
        client.save(new Post(null, "test", "test content", Status.DRAFT, null))
                .log()
                .flatMap(saved -> {
                    var uri = saved.getHeaders().getLocation().toString();
                    var idString = uri.substring(uri.lastIndexOf("/") + 1);
                    log.debug("Post id: {}", idString);
                    return client.getById(UUID.fromString(idString))
                            .log()
                            .map(post -> {
                                log.debug("post: {}", post);
                                return post;
                            });
                })
                .flatMap(post -> {
                    log.debug("getting post: {}", post);
                    return client.update(post.id(), new Post(null, "updated test", "updated content", Status.PENDING_MODERATION, null));
                })
                .subscribe(
                        responseEntity -> log.debug("updated status: {}", responseEntity)
                );

        log.debug("get post by id that not existed.");
        client.getById(UUID.randomUUID())
                .subscribe(
                        post -> log.debug("post: {}", post),
                        error -> log.error("error:", error)
                );
    }
}