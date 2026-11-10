package com.example.demo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class DemoApplicationTests {

    @Autowired
    lateinit var postRepository: PostRepository

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `uses in-memory repository`() {
        assertThat(this.postRepository).isInstanceOf(InMemoryPostRepository::class.java)
    }

    @Test
    fun `get all posts`() {
        val posts = this.client.get().uri("/posts").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .returnResult(Post::class.java)
            .responseBody
            .collectList()
            .block()
        assertThat(posts).hasSizeGreaterThanOrEqualTo(2)
    }

    @Test
    fun `get missing post returns 404`() {
        this.client.get().uri("/posts/${UUID.randomUUID()}").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `crud operations`() {
        val location = this.client.post().uri("/posts").contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Post(title = "Test Title", content = "Test Content"))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().exists("Location")
            .returnResult(Void::class.java)
            .responseHeaders.location
        assertThat(location).isNotNull()
        val path = location!!.path

        this.client.get().uri(path).accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.title").isEqualTo("Test Title")
            .jsonPath("$.content").isEqualTo("Test Content")

        this.client.put().uri(path).contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Post(title = "Updated Title", content = "Updated Content"))
            .exchange()
            .expectStatus().isNoContent

        this.client.get().uri(path).accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.title").isEqualTo("Updated Title")

        this.client.delete().uri(path).accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent

        this.client.get().uri(path).accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }
}