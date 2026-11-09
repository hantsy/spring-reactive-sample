package com.example.demo;

import io.netty.channel.ChannelOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @LocalServerPort
    int port;

    WebClient client;

    @BeforeEach
    public void setup() {
        var reactorHttpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000);
        var clientConnector = new ReactorClientHttpConnector(reactorHttpClient);

        this.client = WebClient.builder()
                .baseUrl("http://localhost:" + this.port)
                .codecs(c -> c.defaultCodecs().enableLoggingRequestDetails(true))
                .defaultHeaders(headers -> headers.set("X-API-Version", "1.0"))
                .apiVersionInserter(ApiVersionInserter.builder()
                                .useHeader("X-API-Version")
                                // .usePathSegment(0)
                                // .useQueryParam("version")
                                // .withVersionFormatter(ApiVersionFormatter)
                                .build()
                )
                .clientConnector(clientConnector)
                .build();
    }

    @Test
    void testHello() {
        this.client.get().uri("/hello")
               // will apply the default version in webclient
               // .apiVersion(1.0)
                .retrieve()
                .bodyToMono(String.class)
                .as(StepVerifier::create)
                .expectNext("Hello v1.0(Default)")
                .verifyComplete();
    }

    @Test
    void testHello1_1() {
        this.client.get().uri("/hello")
                .apiVersion(1.1)
                .retrieve()
                .bodyToMono(String.class)
                .as(StepVerifier::create)
                .expectNext("Hello v1.1")
                .verifyComplete();
    }

    @Test
    void testHello2_0() {
        this.client.get().uri("/hello")
                .apiVersion(2.0)
                .retrieve()
                .bodyToMono(String.class)
                .as(StepVerifier::create)
                .expectNext("Hello v2.0")
                .verifyComplete();
    }

}
