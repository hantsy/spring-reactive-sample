/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author hantsy
 */
public class PostRepositoryTest {

    PostRepository posts;

    @BeforeAll
    public void setup() {
        posts = new PostRepository();
    }

    @Test
    public void testGetAllPosts() {

        StepVerifier.create(posts.findAll())
            .consumeNextWith(p -> assertThat(p.getTitle().equals("post one")).isTrue())
            .consumeNextWith(p -> assertThat(p.getTitle().equals("post two")).isTrue())
            .expectComplete()
            .verify();
    }

}
