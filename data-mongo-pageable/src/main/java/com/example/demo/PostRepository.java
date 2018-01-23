/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface PostRepository extends ReactiveMongoRepository<Post, String>{
    Flux<Post> findByTitleLike(String title, Pageable page);

    Mono<Long> countByTitleLike(String title);
}