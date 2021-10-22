/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** @author hantsy */
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer {

  private final PostRepository postRepository;

  @EventListener(value = ContextRefreshedEvent.class)
  public void init() {
    log.info("start data initialization...");
    this.postRepository
        .save(Post.builder().title("First post title").content("Content of my first post").build())
        .log()
        .thenMany(this.postRepository.findAll().log())
        .subscribe(null, null, () -> log.info("initialization is done..."));
  }
}
