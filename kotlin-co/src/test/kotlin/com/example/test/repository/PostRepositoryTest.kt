package com.example.test.repository

import com.example.demo.domain.Post
import com.example.demo.repository.PostRepository
import com.example.test.repository.PostRepositoryTest.TestConfig
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig(classes = [TestConfig::class])
@ContextConfiguration(initializers = [MongoContextInitializer::class])
class PostRepositoryTest {
    companion object {
        private val log = LoggerFactory.getLogger(PostRepositoryTest::class.java)
    }

    @Configuration
    @ComponentScan(basePackageClasses = [PostRepository::class, Post::class])
    internal class TestConfig

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

