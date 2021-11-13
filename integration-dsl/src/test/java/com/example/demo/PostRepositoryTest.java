/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
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
            .consumeNextWith(p -> assertTrue(p.getTitle().equals("post one")))
            .consumeNextWith(p -> assertTrue(p.getTitle().equals("post two")))
            .expectComplete()
            .verify();
    }

}
