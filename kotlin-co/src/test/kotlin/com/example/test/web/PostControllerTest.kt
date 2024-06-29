package com.example.test.web

import com.example.demo.domain.Post
import com.example.demo.repository.PostRepository
import com.example.test.web.PostControllerTest.TestConfig
import com.example.demo.web.PostController
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*
@SpringJUnitConfig(classes = [TestConfig::class])
class PostControllerTest {
    companion object {
        private val log = LoggerFactory.getLogger(PostControllerTest::class.java)
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = [PostController::class])
    internal class TestConfig {

        @Bean
        @Primary
        fun mockPostRepository(): PostRepository = mockk<PostRepository>()
    }

    @Autowired
    lateinit var posts: PostRepository

    lateinit var client: WebTestClient

    @BeforeEach
    fun setup() {
        log.debug("calling setup...")
        client = WebTestClient.bindToController(PostController(posts))
            .configureClient()
            .build()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `get post by id`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { posts.findById(any()) } returns
                Post(
                    id = id,
                    title = "test title",
                    content = "content of test title"
                )
        client.get().uri("/posts/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.title").isEqualTo("test title")

        coVerify(exactly = 1) { posts.findById(any()) }
    }

}
