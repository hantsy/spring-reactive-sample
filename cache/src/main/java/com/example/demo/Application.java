package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
@ComponentScan(basePackageClasses = Application.class)
@Slf4j
public class Application {
    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(Application.class);
        var posts = context.getBean(PostRepository.class);
        posts.saveAll(
                        List.of(
                                Post.of("What is new in Spring 6.1", "An introduction to new features from Spring 6.1"),
                                Post.of("Spring Boot 3.2", "An introduction to new features in Spring Boot 3.2")
                        )
                )
                .thenMany(posts.findAll())
                .subscribe(post -> log.debug("get the initialized data: {}", post));
        System.out.println("... the end...");
    }
}