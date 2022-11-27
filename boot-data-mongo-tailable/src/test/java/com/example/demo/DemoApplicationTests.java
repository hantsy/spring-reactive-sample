package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class DemoApplicationTests {

    @LocalServerPort
    int port;

    WebClient client;

    @BeforeEach
    void setUp() {
        this.client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .codecs(ClientCodecConfigurer::defaultCodecs)
                .exchangeStrategies(ExchangeStrategies.withDefaults())
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    void testGetAllMessages() throws InterruptedException {
        var verifier = this.client.get().uri("messages").accept(MediaType.TEXT_EVENT_STREAM)
                .exchangeToFlux(it -> it.bodyToFlux(Message.class))
                .as(StepVerifier::create)
                .consumeNextWith(m -> assertThat(m.getBody()).isEqualTo("Welcome!"))
                .consumeNextWith(m -> assertThat(m.getBody()).isEqualTo("message one"))
                .consumeNextWith(m -> assertThat(m.getBody()).isEqualTo("message two"))
                .thenCancel()
                .verifyLater();

        this.client.post().uri("messages").contentType(MediaType.APPLICATION_JSON).bodyValue(new MessageRequest("message one"))
                .exchangeToMono(res -> Mono.just(res.statusCode()))
                .as(StepVerifier::create)
                .consumeNextWith(status -> {
                    assertThat(status).isEqualTo(HttpStatus.CREATED);
                })
                .verifyComplete();

        this.client.post().uri("messages").contentType(MediaType.APPLICATION_JSON).bodyValue(new MessageRequest("message two"))
                .exchangeToMono(res -> Mono.just(res.statusCode()))
                .as(StepVerifier::create)
                .consumeNextWith(status -> {
                    assertThat(status).isEqualTo(HttpStatus.CREATED);
                })
                .verifyComplete();

        // TimeUnit.MILLISECONDS.sleep(500);
        verifier.verify();
    }

}
