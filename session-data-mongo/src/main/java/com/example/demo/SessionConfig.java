/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.session.ReactorSessionRepository;
import org.springframework.session.data.mongo.ReactiveMongoOperationsSessionRepository;
import org.springframework.session.data.mongo.config.annotation.web.reactive.EnableMongoWebSession;

/**
 *
 * @author hantsy
 */
@EnableMongoWebSession
public class SessionConfig {

//    @Bean
//    public ReactorSessionRepository sessionRepository(ReactiveMongoOperations reactiveMongoOperations) {
//        return new ReactiveMongoOperationsSessionRepository(reactiveMongoOperations);
//    }
}
