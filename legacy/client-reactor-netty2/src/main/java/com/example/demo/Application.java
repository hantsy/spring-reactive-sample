package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@ComponentScan(basePackageClasses = Application.class)
@Configuration
@Slf4j
public class Application {
    public static void main(String[] args) {
        var applicationContext = new AnnotationConfigApplicationContext(Application.class);
        var postClient = applicationContext.getBean(PostClient.class);

        postClientExample(postClient);
    }

    static void postClientExample(PostClient client) {
        client.allPosts().subscribe(
                data -> log.debug("post: {}", data)
        );

        client.save(new Post(null, "test", "test content", Status.DRAFT, null))
                .log()
                .flatMap(saved -> {
                            var uri = saved.getHeaders().getLocation().toString();
                            var strings = uri.split("/");
                            return client.getById(UUID.fromString(strings[strings.length - 1]))
                                    .log()
                                    .map(post -> {
                                        log.debug("post: {}", post);
                                        return post;
                                    });
                        }
                )
                .flatMap(post -> {
                    log.debug("getting post: {}", post);
                    return client.update(post.id(), new Post(null, "updated test", "updated content", Status.PENDING_MODERATION, null));
                })
                .subscribe(
                        responseEntity -> log.debug("updated status: {}", responseEntity)
                );
    }
}
