/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

/**
 *
 * @author hantsy
 */
@Configuration
@EnableRedisWebSession
public class SessionConfig {

//    @Bean
//    public ReactiveSessionRepository sessionRepository(ReactiveRedisOperations<String, Object> sessionRedisOperations) {
//        return new ReactiveRedisOperationsSessionRepository(sessionRedisOperations);
//    }
}
