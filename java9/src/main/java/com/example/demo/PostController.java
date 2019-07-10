/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import java.util.concurrent.Flow;

/**
 * @author hantsy
 */
@RestController
@RequestMapping(value = "/posts")
public class PostController {

    @GetMapping
    public Flow.Publisher<Post> all() {
//        Executor proxyExecutor = (Runnable command)-> ForkJoinPool.commonPool().execute(command);
//        SubmissionPublisher publisher  = new SubmissionPublisher(proxyExecutor, Flow.defaultBufferSize());
//        publisher.submit(new Post(1L, "post one", "content of post one"));
//        publisher.submit(new Post(2L, "post two", "content of post two"));
//
//        return publisher;
        // see: https://stackoverflow.com/questions/46597924/spring-5-supports-java-9-flow-apis-in-its-reactive-feature
        return JdkFlowAdapter.publisherToFlowPublisher(
                Flux.just(
                        new Post(1L, "post one", "content of post one"),
                        new Post(2L, "post two", "content of post two")
                )
        );
    }

}
