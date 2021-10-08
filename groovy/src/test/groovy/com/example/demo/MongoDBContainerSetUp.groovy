package com.example.demo

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

abstract class MongoDBContainerSetUp {

    static DockerImageName dockerImageName = DockerImageName.parse("mongo:latest")

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(dockerImageName).withExposedPorts(27017)

    static {
        mongoDBContainer.start()
    }

    @DynamicPropertySource
    static void addProperties(DynamicPropertyRegistry registry) {
        registry.add("mongo.uri", mongoDBContainer::getReplicaSetUrl)
    }
}
