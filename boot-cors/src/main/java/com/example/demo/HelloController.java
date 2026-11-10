package com.example.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    @CrossOrigin(origins = "https://hello-origin.com")
    public Mono<String> hello() {
        return Mono.just("Hello CORS");
    }

}