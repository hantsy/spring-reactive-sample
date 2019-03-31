package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class DemoApplicationTests {

    @LocalServerPort
    private int port;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + this.port)
            .build();
    }

    private MultiValueMap<String, HttpEntity<?>> generateBody() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("fileParts", new ClassPathResource("/foo.txt", DemoApplicationTests.class));
        return builder.build();
    }

    @Test
    public void testUpload() throws IOException {
        byte[] result = client
            .post()
            .uri("/multipart")
            .syncBody(generateBody())
            .exchange()
            .expectStatus().isOk()
            .expectBody().returnResult().getResponseBody();

        ObjectMapper objectMapper = new ObjectMapper();
        Map bodyMap = objectMapper.readValue(result, Map.class);

        String fileId = (String) bodyMap.get("id");
        log.debug("updated file id:" + fileId);
        assertNotNull(fileId);

        client
            .get()
            .uri("/multipart/{id}", fileId)
            .exchange()
            .expectStatus().isOk();

    }

}
