package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
@RequestMapping("/hello")
class GreetingController {

    @GetMapping()
    public String helloDefault() {
        return "Hello v1.0(Default)";
    }

    @GetMapping(version = "1.1")
    public String helloV1_1() {
        return "Hello v1.1";
    }

    @GetMapping(version = "2.0")
    public String helloV2_0() {
        return "Hello v2.0";
    }
}
