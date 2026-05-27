package com.example.demo;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContainersConfig.class)
@Slf4j
public class DemoApplicationTests {


    @Autowired
    WebTestClient client;

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
                .bodyValue(generateBody())
                .exchange()
                .expectStatus().isOk()
                .expectBody().returnResult().getResponseBody();

        ObjectMapper objectMapper = new JsonMapper();
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
