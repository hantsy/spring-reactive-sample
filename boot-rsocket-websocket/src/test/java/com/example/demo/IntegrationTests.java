package com.example.demo;

import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

// The client of the former rsocket-sample websocket/client module, as tests.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTests {

    RSocketRequester requester;

    @LocalServerPort
    int port;

    @Autowired
    RSocketRequester.Builder builder;

    @BeforeAll
    @SuppressWarnings("deprecation")
    public void setup() {
        requester = builder
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .connect(WebsocketClientTransport.create(
                        URI.create("ws://localhost:" + port + "/rsocket"))).block();
    }

    @Test
    public void testRequestResponseOverWebSocket() {
        requester.route("greet.spring")
                .data(new Greeting("Welcome to RSocket"))
                .retrieveMono(String.class)
                .as(StepVerifier::create)
                .consumeNextWith(msg -> assertTrue(msg.contains("Hello spring")))
                .verifyComplete();
    }
}
