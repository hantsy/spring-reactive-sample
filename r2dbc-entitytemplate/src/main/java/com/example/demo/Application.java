package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
@Slf4j
public class Application {

    @Value("${server.port:8080}")
    private int port = 8080;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                Application.class)) {
            var posts = context.getBean(PostRepository.class);
            posts.findAll()
                    .subscribe(
                            data -> log.debug("found post:{}", data),
                            throwable -> log.debug("error caught:{}", throwable.getMessage())
                    );
        }
    }
}