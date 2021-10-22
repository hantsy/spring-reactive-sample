package com.example.demo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [Application::class])
@AutoConfigureWebTestClient
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

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun getPostsShouldBeOK() {
        client
            .get()
            .uri("/posts")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }

}
