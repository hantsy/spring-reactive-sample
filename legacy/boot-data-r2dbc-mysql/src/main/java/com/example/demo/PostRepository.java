/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;


import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

interface PostRepository extends R2dbcRepository<Post, Integer> {

    @Query("SELECT * FROM posts WHERE title like :name")
    Flux<Post> findByTitleContains(String name);

}