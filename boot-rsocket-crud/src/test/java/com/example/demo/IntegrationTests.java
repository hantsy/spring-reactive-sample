package com.example.demo;

import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

// The client of the former rsocket-sample crud/client module, as tests.
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
    public void willLoadPosts() {
        requester.route("posts.findAll")
                .retrieveFlux(Post.class)
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void testTitleContains() {
        requester.route("posts.titleContains")
                .data("one")
                .retrieveFlux(Post.class)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .verifyComplete();
    }

    @Test
    public void testSaveAndGetAndDelete() {
        requester.route("posts.save")
                .data(Post.builder().title("test").content("content of test").build())
                .retrieveMono(Post.class)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("test", p.getTitle()))
                .verifyComplete();

        Integer id = requester.route("posts.findAll")
                .retrieveFlux(Post.class)
                .filter(p -> "test".equals(p.getTitle()))
                .map(Post::getId)
                .next()
                .block();

        requester.route("posts.deleteById." + id)
                .send()
                .as(StepVerifier::create)
                .verifyComplete();
    }
}
