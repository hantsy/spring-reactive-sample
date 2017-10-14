/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.session.ReactorSessionRepository;
import org.springframework.session.data.redis.ReactiveRedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.reactor.EnableRedisReactorSession;

/**
 *
 * @author hantsy
 */
@EnableRedisReactorSession
public class SessionConfig {

//    @Bean
//    public ReactorSessionRepository sessionRepository(ReactiveRedisOperations<String, Object> sessionRedisOperations) {
//        return new ReactiveRedisOperationsSessionRepository(sessionRedisOperations);
//    }
}
