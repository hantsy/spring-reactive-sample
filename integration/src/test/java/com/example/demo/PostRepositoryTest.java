/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import reactor.test.StepVerifier;

/**
 *
 * @author hantsy
 */
public class PostRepositoryTest {

    PostRepository posts;

    @Before
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
