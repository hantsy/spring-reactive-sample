package com.example.demo;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 *
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")
public class ApplicationTests {

//    @Autowired
//    ApplicationContext context;

    @Autowired
    RouterFunction<ServerResponse> routerFunction;

    WebTestClient client;

    @BeforeAll
    public void setup() {
        this.client = WebTestClient
           // .bindToApplicationContext(this.context)
            .bindToRouterFunction(this.routerFunction)
            .configureClient()
            .build();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.client
            .get()
            .uri("/posts")
            .exchange()
            .expectStatus().isOk();  
    }

}
