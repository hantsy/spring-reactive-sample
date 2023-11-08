package com.example.demo;


import lombok.SneakyThrows;
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
    @SneakyThrows
    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(Application.class);

        var counter = context.getBean(ReactiveCounter.class);
        log.debug("counter is:  {}", counter.getInvocationCount().block());
        Thread.sleep(100);
        log.debug("counter value after 100ms is:  {}", counter.getInvocationCount().block());
        System.out.println("... the end...");
    }
}