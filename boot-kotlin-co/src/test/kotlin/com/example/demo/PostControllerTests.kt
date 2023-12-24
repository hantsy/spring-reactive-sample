package com.example.demo


import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.util.*

@WebFluxTest(
    controllers = [PostController::class],
    excludeAutoConfiguration = [
        ReactiveUserDetailsServiceAutoConfiguration::class,
        ReactiveSecurityAutoConfiguration::class
    ]
)
class PostControllerTests {

    @Autowired
    private lateinit var client: WebTestClient

    @MockkBean
    private lateinit var posts: PostRepository

    @BeforeAll
    fun setup() {
        println(">> setup testing...")
    }

    @Test
    fun `get all posts`() = runTest {
        coEvery { posts.findAll() } returns
                flowOf(
                    Post(id = "1", title = "post one", content = "content of post one"),
                    Post(id = "2", title = "post two", content = "content of two")
                )
        client.get()
            .uri("/posts")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.size()").isEqualTo(2)

        coVerify(exactly = 1) { posts.findAll() }
    }

    @Test
    fun `get post by id`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { posts.findById(any()) } returns
                Post(
                    id = id,
                    title = "test",
                    content = "content of test",
                    createdDate = LocalDateTime.now()
                )

        client.get()
            .uri("/posts/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.title").isEqualTo("test")

        coVerify(exactly = 1) { posts.findById(any()) }
    }

    @Test
    fun `get post by id when not found`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { posts.findById(any()) } returns null

        client.get()
            .uri("/posts/$id")
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { posts.findById(any()) }
    }

}
