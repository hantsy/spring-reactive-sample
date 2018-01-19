/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.epages.restdocs.raml.RamlResourceSnippetParameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.restdocs.JUnitRestDocumentation;

import static com.epages.restdocs.raml.RamlResourceDocumentation.ramlResource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author hantsy
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class ApiDocumentation {

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @Before
    public void setup() {
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
            .expectBody().consumeWith(
            document(
                "posts",
                ramlResource(
                    RamlResourceSnippetParameters.builder()
                        .description("Get all posts")
                        .responseFields(
                            fieldWithPath("title").description("The title of post"),
                            fieldWithPath("content").description("The content of post")
                        )
                        .build()
                )
            )
        );
    }

}
