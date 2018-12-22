package com.example.demo


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.test

@SpringBootTest(classes = [DemoApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = arrayOf(TestConfigInitializer::class))
class IntegrationTests {

    private lateinit var client: WebClient

    @LocalServerPort
    private var port: Int = 8080

    @BeforeAll
    fun setup() {
        client = WebClient.create("http://localhost:$port")
    }

    @Test
    fun `get all posts`() {
        client.get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.OK }
                .verifyComplete()
    }

}
