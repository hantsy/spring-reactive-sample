/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hantsy
 */
@RestController
@RequestMapping(value = "/posts")
public class PostController {

    @GetMapping
    public Flow.Publisher<Post> all() {
        SubmissionPublisher publisher = new SubmissionPublisher();
        publisher.submit(new Post(1L, "post one", "content of post one"));
        publisher.submit(new Post(2L, "post two", "content of post two"));

        return publisher;
    }

}
