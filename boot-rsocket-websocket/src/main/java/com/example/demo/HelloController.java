package com.example.demo;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping
    public String get() {
        return "server greeting at :" + Instant.now();
    }
}
