package com.example.demo;

import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

// The client of the former rsocket-sample tcp/client module, as tests.
@SpringBootTest
public class IntegrationTests {

    static RSocketRequester requester;

    @BeforeAll
    public static void setup(@Autowired RSocketRequester.Builder builder) {
        // the rsocket server port is fixed in application.properties (7000)
        requester = builder
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .connect(TcpClientTransport.create("localhost", 7000)).block();
    }

    @Test
    public void testFireAndForget() {
        requester.route("hello")
                .data(new Greeting("Welcome to RSocket"))
                .send()
                .block();
        assertTrue(true);
    }

    @Test
    public void testRequestResponse() {
        requester.route("greet.spring")
                .data(new Greeting("Welcome to RSocket"))
                .retrieveMono(String.class)
                .as(StepVerifier::create)
                .consumeNextWith(msg -> assertTrue(msg.contains("Hello spring")))
                .verifyComplete();
    }

    @Test
    public void testRequestStream() {
        requester.route("greet-stream")
                .data(new Greeting("Welcome to RSocket"))
                .retrieveFlux(String.class)
                .take(3)
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void testRequestChannel() {
        Flux<Greeting> payloads = Flux.range(0, 10)
                .map(i -> new Greeting("Welcome to RSocket #" + i));

        requester.route("greet-channel")
                .data(payloads)
                .retrieveFlux(String.class)
                .take(10)
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();
    }
}
