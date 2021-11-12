package com.example.demo


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(classes = [DemoApplication::class])
class ApplicationTests {

    @Autowired
    private lateinit var controller: PostController

    private lateinit var client: WebTestClient


    @BeforeAll
    fun setup() {
        client = WebTestClient.bindToController(controller).build()
    }

    @Test
    fun `get all posts`() {
        client.get().uri("/posts")
            .exchange()
            .expectStatus().isOk
    }

}
