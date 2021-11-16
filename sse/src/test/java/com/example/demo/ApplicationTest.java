package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;


@SpringJUnitConfig(Application.class)
public class ApplicationTest {

  @Value("${server.port:8080}")
  private int port;

  @Autowired
  private HttpServer httpServer;

  private DisposableServer server;
  private WebClient client;

  @BeforeEach
  void setUp() {
    this.server = this.httpServer.bindNow();
    this.client = WebClient.builder()
        .baseUrl("http://localhost:" + port)
        .build();
  }

  @AfterEach
  void tearDown() {
    if (!this.server.isDisposed()) {
      this.server.disposeNow();
    }
  }

  @Test
  void getMessageStream() {
    var verifier = this.client.get().uri("messages").accept(MediaType.TEXT_EVENT_STREAM)
        .exchangeToFlux(it -> it.bodyToFlux(Message.class))
        .as(StepVerifier::create)
        .consumeNextWith(m -> assertThat(m.getBody()).isEqualTo("message one"))
        .consumeNextWith(m -> assertThat(m.getBody()).isEqualTo("message two"))
        .thenCancel()
        .verifyLater();

    this.client.post().uri("messages").contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new MessageRequest("message one"))
        .exchangeToMono(it -> Mono.just(it.statusCode()))
        .as(StepVerifier::create)
        .consumeNextWith(s -> assertThat(s).isEqualTo(HttpStatus.CREATED))
        .expectComplete()
        .verify();

    this.client.post().uri("messages").contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new MessageRequest("message two"))
        .exchangeToMono(it -> Mono.just(it.statusCode()))
        .as(StepVerifier::create)
        .consumeNextWith(s -> assertThat(s).isEqualTo(HttpStatus.CREATED))
        .expectComplete()
        .verify();

    verifier.verify();
  }
}