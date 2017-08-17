/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.io.IOException;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * @author hantsy
 */
public class DemoClient {

    public static final void main(String[] args) throws IOException {
        WebClient client = WebClient.create("http://localhost:8080");
        client
            .get()
            .uri("/posts")
            .exchange()
            .flatMapMany(res -> res.bodyToFlux(Post.class))
            .log()
            .subscribe(post -> System.out.println("post: " + post));

        System.out.println("Client is started!");
        System.in.read();
    }
}
