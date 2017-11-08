/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.session.MapReactiveSessionRepository;
import org.springframework.session.ReactiveSessionRepository;

/**
 *
 * @author hantsy
 */
@EnableSpringWebSession
public class SessionConfig {

    @Bean
    public ReactiveSessionRepository sessionRepository() {
        return new MapReactiveSessionRepository( new ConcurrentHashMap<>());
    }
}
