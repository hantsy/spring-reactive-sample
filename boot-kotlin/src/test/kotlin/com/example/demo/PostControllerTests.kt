package com.example.demo


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux

@WebFluxTest(
        controllers = [PostController::class],
        excludeAutoConfiguration = [ReactiveUserDetailsServiceAutoConfiguration::class, ReactiveSecurityAutoConfiguration::class]
)
class PostControllerTests {

    @Autowired
    private lateinit var client: WebTestClient

    @MockBean
    private lateinit var posts: PostRepository

    @BeforeAll
    fun setup() {
        println(">> setup testing...")
    }


    @Test
    fun `get all posts`() {
        val postsFlux = Flux.just("Post one", "Post two")
                .map {
                    Post(title = it, content = "content of $it")
                }
        given(posts.findAll()).willReturn(postsFlux)
        client.get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .isOk
        verify(posts, times(1)).findAll()
        verifyNoMoreInteractions(this.posts)
    }

}
