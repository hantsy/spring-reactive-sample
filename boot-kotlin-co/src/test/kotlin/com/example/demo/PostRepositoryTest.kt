package com.example.demo

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

@DataMongoTest
@Import(TestcontainersConfiguration::class)
class PostRepositoryTest {
    companion object {
        private val log = LoggerFactory.getLogger(PostRepositoryTest::class.java)
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