/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;

/**
 *
 * @author hantsy
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    @Async
    public CompletableFuture<List<Post>> readAllBy();

}
