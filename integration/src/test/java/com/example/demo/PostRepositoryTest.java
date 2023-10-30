/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author hantsy
 */
public class PostRepositoryTest {

    PostRepository posts;

    @BeforeEach
    public void setup() {
        posts = new PostRepository();
    }

    @Test
    public void testGetAllPosts() {

        StepVerifier.create(posts.findAll())
                .consumeNextWith(p -> assertEquals("post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("post two", p.getTitle()))
                .expectComplete()
                .verify();
    }

}
