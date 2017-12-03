package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@WebFluxTest
public class DemoApplicationTests {
    
    @Autowired
    WebTestClient client;

	@Test
	public void getAllMessagesShouldBeOk() {
        client.get().uri("/").exchange().expectStatus().isOk();
	}

}
