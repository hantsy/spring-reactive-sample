package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
//import org.springframework.boot.webtestclient.WebTestClientBuilderCustomizer;
//4.0-rc2 changed the package
import org.springframework.boot.webtestclient.autoconfigure.WebTestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.ApiVersionInserter;

@WebFluxTest(controllers = GreetingController.class)
public class GreetingControllerTest {

    @TestConfiguration
    static class TestConfig {
      
        @Bean
        WebTestClientBuilderCustomizer testClientBuilderCustomizer() {
            return builder -> builder.defaultApiVersion("1.0")
                    .apiVersionInserter(ApiVersionInserter.useHeader("X-API-Version"))
                    .build();
        }
    }

    @Autowired
    WebTestClient webTestClient;

//    WebTestClient webTestClient;
//
//    @Autowired GreetingController greetingController;
//
//    @BeforeEach
//    public void setup() {
//        this.webTestClient = WebTestClient
//                .bindToController(greetingController)
//                .apiVersioning(apiVersionConfigurer ->
//                        apiVersionConfigurer.useRequestHeader("X-API-Version")
//                                .setDefaultVersion("1.0")
//                )
//                .configureClient()
//                .apiVersionInserter(ApiVersionInserter.builder()
//                        .useHeader("X-API-Version")
//                        .build())
//                .build();
//    }

    @Test
    void testHello() {
        this.webTestClient.get().uri("/hello")
                // .apiVersion("1.0")
                .exchange()
                .expectBody(String.class).isEqualTo("Hello v1.0(Default)");
    }

    @Test
    void testHello1_1() {
        this.webTestClient.get().uri("/hello")
                .apiVersion("1.1")
                .exchange()
                .expectBody(String.class).isEqualTo("Hello v1.1");
    }

    @Test
    void testHello2_0() {
        this.webTestClient.get().uri("/hello")
                .apiVersion("2.0")
                .exchange()
                .expectBody(String.class).isEqualTo("Hello v2.0");
    }
}
