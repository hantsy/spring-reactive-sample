package com.example.demo


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

@SpringBootTest
@ContextConfiguration(initializers = [TestConfigInitializer::class])
@Import(TestcontainersConfiguration::class)
class ApplicationTests {

    @Autowired
    private lateinit var routerFunction: RouterFunction<ServerResponse>

    private lateinit var client: WebTestClient

    @BeforeAll
    fun setup() {
        client = WebTestClient.bindToRouterFunction(routerFunction)
            .configureClient()
            .build()
    }

    @Test
    fun `get all posts`() {
        client.get().uri("/posts")
            .exchange()
            .expectStatus().isOk
    }

}
