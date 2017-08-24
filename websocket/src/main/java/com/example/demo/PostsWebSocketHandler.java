/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 *
 * @author hantsy
 */
public class PostsWebSocketHandler implements WebSocketHandler {

    private final PostRepository posts;

    public PostsWebSocketHandler(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public List<String> getSubProtocols() {
        return Arrays.asList("test");
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String protocol = session.getHandshakeInfo().getSubProtocol();
        WebSocketMessage message = session.textMessage(this.posts.findAll().takeLast(0).toString());
        return doSend(session, Mono.just(message));
    }

    // TODO: workaround for suspected RxNetty WebSocket client issue
    // https://github.com/ReactiveX/RxNetty/issues/560
    private Mono<Void> doSend(WebSocketSession session, Publisher<WebSocketMessage> output) {
        return session.send(Mono.delay(Duration.ofMillis(100)).thenMany(output));
    }

}
