package com.example.demo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.test

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [Application::class])
@Testcontainers
class ApplicationTests {

    companion object {

        private val dockerImageName: DockerImageName = DockerImageName.parse("mongo:latest")

        @Container
        private val mongoDBContainer: MongoDBContainer = MongoDBContainer(dockerImageName)
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun addProperties(registry: DynamicPropertyRegistry) {
            registry.add("mongo.uri", mongoDBContainer::getReplicaSetUrl)
        }
    }

    lateinit var client: WebClient

    @BeforeEach
    fun setup() {
        client = WebClient.create("http://localhost:8080")
    }

    @Test
    fun getPostsShouldBeOK() {
        client
                .get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.OK }
                .verifyComplete()
    }

}
