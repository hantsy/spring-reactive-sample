package com.example.demo;

import io.netty.channel.ChannelOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringJUnitConfig(classes = Application.class)
public class IntegrationTests {

    @Value("${server.port:8080}")
    int port;

    WebClient client;

    @Autowired
    HttpServer httpServer;

    private DisposableServer disposableServer;

    @BeforeEach
    public void setup() {
        var reactorHttpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000);
        var clientConnector = new ReactorClientHttpConnector(reactorHttpClient);

        this.disposableServer = this.httpServer.bindNow(Duration.ofMillis(5_000));
        this.client = WebClient.builder()
                .baseUrl("http://localhost:" + this.port)
                .codecs(c -> c.defaultCodecs().enableLoggingRequestDetails(true))
                //.defaultHeaders(headers -> headers.set("X-API-Version", "1.0"))
                .apiVersionInserter(ApiVersionInserter.builder()
                                .useHeader("X-API-Version")
//                        .usePathSegment(0)
//                        .useQueryParam("version")
                                // .withVersionFormatter(ApiVersionFormatter)
                                .build()
                )
                .clientConnector(clientConnector)
                .build();
    }

    @AfterEach
    public void teardown() {
        this.disposableServer.disposeNow();
    }

    @Test
    public void testHello() {
        this.client.get().uri("/hello")
                .apiVersion("1.0")
                .retrieve()
                .bodyToMono(String.class)
                .as(StepVerifier::create)
                .expectNext("Hello v1.0(Default)")
                .verifyComplete();
    }

    @Test
    public void testHello1_1() {
        this.client.get().uri("/hello")
                .apiVersion("1.1")
                .retrieve()
                .bodyToMono(String.class)
                .as(StepVerifier::create)
                .expectNext("Hello v1.1")
                .verifyComplete();
    }

    @Test
    public void testHello2_0() {
        this.client.get().uri("/hello")
                .apiVersion("2.0")
                .retrieve()
                .bodyToMono(String.class)
                .as(StepVerifier::create)
                .expectNext("Hello v2.0")
                .verifyComplete();
    }

}
