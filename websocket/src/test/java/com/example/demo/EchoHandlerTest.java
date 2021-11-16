package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringJUnitConfig(Application.class)
@Slf4j
public class EchoHandlerTest {

    @Value("${server.port:8080}")
    private int port;

    @Autowired
    private HttpServer httpServer;

    @Autowired
    private ObjectMapper objectMapper;

    private DisposableServer server;

    private WebSocketClient client;

    @BeforeEach
    void setUp() {
        this.server = this.httpServer.bindNow();
        this.client = new ReactorNettyWebSocketClient();
    }

    @AfterEach
    void tearDown() {
        if (!this.server.isDisposed()) {
            this.server.disposeNow();
        }
    }

    @SneakyThrows
    @Test
    void getMessageStream() {

        var replayList = new ArrayList<String>();
        var latch = new CountDownLatch(1);

        var socketUri = URI.create("ws://localhost:" + port + "/echo");

        WebSocketHandler handler = session -> {

            var receiveMono = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log("client receiving::")
                    .doOnNext(replayList::add)
                    .then();

            var sendMono = session
                    .send(
                            Mono.delay(Duration.ofMillis(100)).thenMany(
                                    Flux.just("message one", "message two").map(session::textMessage)
                            )
                    )
                    .doOnSubscribe(subscription -> log.debug("session is open"))
                    .doOnTerminate(() -> log.debug("session is closing"))
                    .log("client sending::");

            return sendMono.then(receiveMono);
        };

        this.client.execute(socketUri, handler)
                .doOnTerminate(latch::countDown)
                .subscribe();

        latch.await(1000L, TimeUnit.MILLISECONDS);
        assertThat(replayList.size()).isEqualTo(2);
        assertThat(replayList).containsSequence("received:message one", "received:message two");
    }


}