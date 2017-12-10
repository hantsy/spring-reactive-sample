package com.example.demo

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.test

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = arrayOf(Application::class))
class ApplicationTests {

    @Value("#{@nettyContext.address().getPort()}")
    var port = 8080

    lateinit var client: WebClient

    @Before
    fun setup() {
        client = WebClient.create("http://localhost:8080")
    }

    @Test
    fun getPostsShouldBeOK() {
        client
                .get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.OK }
                .verifyComplete()
    }

}
