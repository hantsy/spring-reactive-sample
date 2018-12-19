package com.example.demo


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(classes = arrayOf(DemoApplication::class))
class ApplicationTests {

    @Autowired
    private lateinit var routing: PostRoutes

    private lateinit var client: WebTestClient


    @BeforeAll
    fun setup(){
        client = WebTestClient.bindToRouterFunction(routing.routes()).build()
    }


    @Test
    fun `get all posts`() {
        client.get().uri("/posts")
                .exchange().expectStatus().isOk
    }

}
