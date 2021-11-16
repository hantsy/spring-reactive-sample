/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author hantsy
 */
@RequiredArgsConstructor
public class MessagetHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;

    private Sinks.Many<Message> sinks = Sinks.many().replay().limit(2);
    private Flux<Message> outputMessages = sinks.asFlux();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        var receiveMono = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::readIncomingMessage)
                .map(req -> Message.builder().id(UUID.randomUUID()).body(req.message()).sentAt(LocalDateTime.now()).build())
                .log("server receiving::")
//                .subscribe(
//                        data -> sinks.emitNext(data, Sinks.EmitFailureHandler.FAIL_FAST),
//                        error -> sinks.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST)
//                );
                .doOnNext(data -> sinks.emitNext(data, Sinks.EmitFailureHandler.FAIL_FAST))
                .doOnError(error -> sinks.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST))
                .then();

        // TODO: workaround for suspected RxNetty WebSocket client issue
        // https://github.com/ReactiveX/RxNetty/issues/560
        var sendMono = session
                .send(
                        Mono.delay(Duration.ofMillis(500))
                                .thenMany(outputMessages.map(msg -> session.textMessage(toJsonString(msg))))
                )
                .log("server sending::")
                .onErrorResume(throwable -> session.close())
                .then();

        return Mono.zip(receiveMono, sendMono).then();
        //return sendMono;
    }

    @SneakyThrows
    private String toJsonString(Message msg) {
        return objectMapper.writeValueAsString(msg);
    }

    @SneakyThrows
    private MessageRequest readIncomingMessage(String text) {
        return objectMapper.readValue(text, MessageRequest.class);
    }

}
