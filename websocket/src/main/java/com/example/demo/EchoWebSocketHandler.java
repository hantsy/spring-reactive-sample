/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
public class EchoWebSocketHandler implements WebSocketHandler {

    public EchoWebSocketHandler() {
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Use retain() for Reactor Netty
        return session.send(session.receive().doOnNext(WebSocketMessage::retain));
    }
}
