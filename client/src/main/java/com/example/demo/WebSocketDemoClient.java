/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

/**
 *
 * @author hantsy
 */
@Slf4j
public class WebSocketDemoClient {

    public static final void main(String[] args) throws URISyntaxException {

        WebSocketClient client = new ReactorNettyWebSocketClient();
//        client.execute(new URI("ws://localhost:8080/echo"), (WebSocketSession session) -> {
//            session.send().log().;
//        });
        
        int count = 100;
		Flux<String> input = Flux.range(1, count).map(index -> "msg-" + index);
		ReplayProcessor<Object> output = ReplayProcessor.create(count);

		client.execute(new URI("ws://localhost:8080/echo"),
				session -> {
					log.debug("Starting to send messages");
					return session
							.send(input.doOnNext(s -> log.debug("outbound " + s)).map(session::textMessage))
							.thenMany(session.receive().take(count).map(WebSocketMessage::getPayloadAsText))
							.subscribeWith(output)
							.doOnNext(s -> log.debug("inbound " + s))
							.then()
							.doOnTerminate((aVoid, ex) ->
									log.debug("Done with " + (ex != null ? ex.getMessage() : "success")));
				})
				.block(Duration.ofMillis(5000));

//		assertEquals(input.collectList().block(Duration.ofMillis(5000)),
//				output.collectList().block(Duration.ofMillis(5000)));
//        client.execute(new URI("ws://localhost:8080/echo")), session -> {
//            session.
//        }
//        ).blockMillis(5000);
    }
}
