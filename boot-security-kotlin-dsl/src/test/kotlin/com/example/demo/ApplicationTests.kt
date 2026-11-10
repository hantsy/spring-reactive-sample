package com.example.demo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApplicationTests {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `get all posts anonymously`() {
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
    fun `create a post without auth fails with 401`() {
        this.client.post().uri("/posts").contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Post(title = "test post", content = "test content"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `create a post with basic auth`() {
        this.client.post().uri("/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .headers { it.setBasicAuth("user", "password") }
            .bodyValue(Post(title = "test post", content = "test content"))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().exists("Location")
    }

    @Test
    fun `delete requires admin role`() {
        val location = this.client.post().uri("/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .headers { it.setBasicAuth("user", "password") }
            .bodyValue(Post(title = "to delete", content = "content"))
            .exchange()
            .expectStatus().isCreated
            .returnResult(Void::class.java)
            .responseHeaders.location
        assertThat(location).isNotNull()
        val path = location!!.path

        this.client.delete().uri(path)
            .headers { it.setBasicAuth("user", "password") }
            .exchange()
            .expectStatus().isForbidden

        this.client.delete().uri(path)
            .headers { it.setBasicAuth("admin", "password") }
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `user info requires authentication`() {
        this.client.get().uri("/auth/user").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized

        this.client.get().uri("/auth/user").accept(MediaType.APPLICATION_JSON)
            .headers { it.setBasicAuth("user", "password") }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.user").isEqualTo("user")
            .jsonPath("$.roles[0]").isEqualTo("ROLE_USER")
    }
}