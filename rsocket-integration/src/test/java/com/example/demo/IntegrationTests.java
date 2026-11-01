package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

// The client of the former rsocket-sample integration/client module, as tests.
@SpringBootTest
public class IntegrationTests {

    @Autowired
    @Qualifier("rsocketUpperCaseRequestFlow.gateway")
    Function<Flux<String>, Flux<String>> uppercaseFlow;

    @Autowired
    ClientRSocketConnector clientRSocketConnector;

    @BeforeEach
    public void startClient() {
        if (!clientRSocketConnector.isRunning()) {
            clientRSocketConnector.start();
        }
    }

    @Test
    public void testUppercaseFlow() {
        uppercaseFlow.apply(Flux.just("a", "b", "c", "d"))
                .as(StepVerifier::create)
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
    }
}
