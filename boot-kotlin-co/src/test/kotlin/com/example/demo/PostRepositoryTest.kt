package com.example.demo

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
@DataMongoTest
class PostRepositoryTest {
    companion object {
        private val log = LoggerFactory.getLogger(PostRepositoryTest::class.java)

        @Container
        val mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo")

        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") {
                mongoDBContainer.replicaSetUrl
            }
        }

    }

    @Autowired
    lateinit var posts: PostRepository

    @Autowired
    lateinit var template: ReactiveMongoTemplate

    @BeforeEach
    fun setup() = runTest {
        log.debug("calling setup...")
        posts.deleteAll()
    }

    @Test
    fun `post repository is present in context`() = runTest {
        posts shouldNotBe null
    }

    @Test
    fun `test save and query`() = runTest {
        val saved = template.insert(Post(title = "test", content = "content of test")).awaitSingle()
        saved.id shouldNotBe null

        val postById = posts.findById(saved.id!!)
        postById shouldNotBe null
        postById!!.title shouldBe "test"
    }

}