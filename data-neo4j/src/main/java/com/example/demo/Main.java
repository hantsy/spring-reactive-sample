package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.template.TemplatePostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class Main {
    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext("com.example.demo");
        var posts = context.getBean(TemplatePostRepository.class);

        posts.save(Post.builder().title("first post").build())
                .thenMany(posts.findAll())
                .subscribe(
                        data -> log.info("data: {}", data),
                        err -> log.error("error: {}", err.getMessage()),
                        () -> log.info("done")
                );
    }
}
