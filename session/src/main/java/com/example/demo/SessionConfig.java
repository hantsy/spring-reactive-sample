/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author hantsy
 */
@EnableSpringWebSession
public class SessionConfig {

    @Bean
    public ReactiveSessionRepository<?> sessionRepository() {
        return new ReactiveMapSessionRepository( new ConcurrentHashMap<>());
    }
}
