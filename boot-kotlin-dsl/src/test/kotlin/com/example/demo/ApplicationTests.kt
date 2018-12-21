package com.example.demo


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux

@SpringBootTest(classes = arrayOf(DemoApplication::class))
class ApplicationTests {

    @Autowired
    private lateinit var routing: PostRoutes

    @MockBean
    private lateinit var handler: PostHandler

    private lateinit var client: WebTestClient


    @BeforeAll
    fun setup() {
        client = WebTestClient.bindToRouterFunction(routing.routes()).configureClient().build()
    }


    @Test
    fun `get all posts`() {
        val postsFlux = Flux.just("Post one", "Post two")
                .map {
                    Post(title = it, content = "content of $it")
                }
        given(handler.all(any(ServerRequest::class.java)))
                .willReturn(ServerResponse.ok().syncBody(postsFlux))

        client.get().uri("/posts")
                .exchange().expectStatus().isOk
    }

}
