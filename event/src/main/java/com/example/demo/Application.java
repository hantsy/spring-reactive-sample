package com.example.demo;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@Configuration
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
@ComponentScan(basePackageClasses = Application.class)
@Slf4j
public class Application {
    @SneakyThrows
    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(Application.class);

        var publisher  = context.getBean(GreetingPublisher.class);
        var listener = context.getBean(GreetingListener.class);
        listener.sinks.asFlux().subscribe(g -> log.debug("subscribed event: {}", g));

        IntStream.range(1, 100)
                        .forEach(i -> {
                            publisher.publishGreetingEvent("hello world #"+i +" at " + LocalDateTime.now());

                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });


        System.out.println("... the end...");
    }
}