/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.restdocs.JUnitRestDocumentation;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author hantsy
 */
@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
public class ApiDocumentation {

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentation) {
        this.client = WebTestClient
            .bindToApplicationContext(this.context)
            .configureClient()
            .baseUrl("https://api.example.com")
            .filter(documentationConfiguration(restDocumentation))
            .build();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.client
            .get()
            .uri("/posts")
            .exchange()
            .expectStatus().isOk()
            .expectBody().consumeWith(document("sample"));
    }

}
