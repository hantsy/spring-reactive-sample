/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.session.EnableSpringWebSession;
import org.springframework.session.MapReactorSessionRepository;
import org.springframework.session.ReactorSessionRepository;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

/**
 *
 * @author hantsy
 */
@EnableSpringWebSession
public class SessionConfig {

    @Bean
    public ReactorSessionRepository sessionRepository() {
        return new MapReactorSessionRepository(new ConcurrentHashMap<>());
    }

    @Bean
    public WebSessionIdResolver headerWebSessionIdResolver() {
        HeaderWebSessionIdResolver resolver = new HeaderWebSessionIdResolver();
        resolver.setHeaderName("X-SESSION-ID");
        return resolver;
    }
}
