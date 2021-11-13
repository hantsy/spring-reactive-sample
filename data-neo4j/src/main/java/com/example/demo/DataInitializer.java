/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.template.TemplatePostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hantsy
 */
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer {
    private final TemplatePostRepository templatePostRepository;

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");
        this.templatePostRepository
                .deleteAll()
                .thenMany(
                        this.templatePostRepository.saveAll(
                                List.of(
                                        Post.builder().title("post one").content("The content of post one").build(),
                                        Post.builder().title("post two").content("The content of post two").build()
                                )
                        )
                )
                .subscribe(post -> log.debug("The initialization is done...."));

    }

}
