# Testing Spring Data Neo4j with Testcontainers

Spring Boot provides a series of `AutoConfigureXXX` to allow developers to test against databases in an isolated environment. 

For example,  adding a test scoped H2 dependency into your project and annotating your test class with `@DataJpaTest`, you are allowed to test your repository class against an embedded H2 instead of the runtime database. With the `@DataJpaTest`, Spring test context only loads the essential configuration for testing JPA facilities.  

For Spring Data Neo4j, Spring Boot also provides a `@DataNeo4jTest` for testing Neo4j facilities, but unfortunately there is no simple embedded solution to start up a Neoj4 database.  [Testcontainers](https://www.testcontainers.org) fills the blank table.

Testcontainers provides a simple programmatic API abstraction for you to bootstrap a Docker container in your testing codes.

You can add *TestContainers* as dependencies when generating new project in the [Spring initializr](https://start.spring.io).

Or add the following dependencies into your *pom.xml* manually.

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>neo4j</artifactId>
    <scope>test</scope>
</dependency>
```

In the above code, 

* The `junit-jupiter` is used to integrate *TestContainers* with JUnit 5 platform.
* The `neo4j` provides APIs to compose a Neo4j Docker container.

Create a test for `PostRepository`.

```java
@SpringBootTest
//@DataNeo4jTest
@Testcontainers
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PostRepositoryWithTestContainersTest {

    @Container
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.0")
            .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }


    @Autowired
    private PostRepository posts;

    @BeforeEach
    public void setup() throws IOException {
        log.debug("running setup.....,");
        this.posts.deleteAll()
                .thenMany(testSaveMethod())
                .log()
                .thenMany(testFoundMethod())
                .log()
                .blockLast(Duration.ofSeconds(5));// to make the tests work
    }

    private Flux<Post> testSaveMethod() {
        var data = Stream.of("Post one", "Post two")
                .map(title -> Post.builder().title(title).content("The content of " + title).build())
                .collect(Collectors.toList());
        return Flux.fromIterable(data)
                .flatMap(it -> this.posts.save(it));
    }

    private Flux<Post> testFoundMethod() {
        return this.posts
                .findAll(Example.of(Post.builder().title("Post one").build()));
    }

    @AfterEach
    void teardown() {
    }

    @Test
    void testAllPosts() {
        posts.findAll().sort(Comparator.comparing(post -> post.getTitle()))
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
                .consumeNextWith(p -> assertEquals("Post two", p.getTitle()))
                .verifyComplete();
    }

}
```

In the above codes, 

*  A test class is annotated with a general `@SpringBootTest` annotation(will load all configurations) or a `@DataNeo4jTest` annotation. When using `@DataNeo4jTest`, you have to add an extra `@Transactional(propagation = Propagation.NEVER)`, check  [spring-boot issue#23630](https://github.com/spring-projects/spring-boot/issues/23630) for more details.
* A `@Testcontainers` is added on the class level, thus the Testcontainers facilities will contribute the test lifecycle.
* A static `@Container` resource is defined, it will be initialized before the test execution.
*  By default, JUnit 5 uses a `PER_METHOD` strategy to bootstrap a test, if you set a global strategy in the *junit-platform.properties*, add a `@TestInstance(TestInstance.Lifecycle.PER_METHOD)` to override it.
* A static method annotated with `@DynamicPropertySource` is used to bind parameters from the running Docker container to the Spring environmental variables before the test is running. 
* You can inject Repository class, and Neo4j specific `Driver` etc,  directly.
* Generally, you can add `@BeforeEach`, `@AfterEach` methods to hook the test lifecycle.
* In the `@Test` method, we usually utilizes reactor's `StepVerifier` to assert the result.

> In the original [SDN Rx]((https://github.com/neo4j/sdn-rx/)  ), it provided a `@ReactiveDataNeo4jTest` for testing reactive applications, this annotation is not available in Spring Boot 2.4.

Run the test, it will:

* Preparing Spring test context.
* Check if there is a Docker image existed, if not download it firstly.
* Startup a Docker container for the test.
* Bind the Docker instance parameters to Spring environmental variables. 
* Inject Spring resources. 
* Executing test, if there are some hooks, executing hooks before and after the test execution.
* Shutdown the Docker container and clean up Spring test context. 

Alternatively, you can create a `ApplicationContextInitializer` to start a Neo4j Docker container.

```java
@DataNeo4jTest
@Transactional(propagation = Propagation.NEVER)
@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
@Slf4j
public class PostRepositoryTest {


    static class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.0").withoutAuthentication();
            neo4jContainer.start();
            configurableApplicationContext
                    .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> neo4jContainer.stop());
            TestPropertyValues
                    .of(
                            "spring.neo4j.uri=" + neo4jContainer.getBoltUrl(),
                            "spring.neo4j.authentication.username=neo4j",
                            "spring.neo4j.authentication.password=" + neo4jContainer.getAdminPassword()
                    )
                    .applyTo(configurableApplicationContext.getEnvironment());
        }
    }

...
}
```

In the above, we use a `ContextConfiguration` to apply the context initializers.

Grab the [source code]() from my github.

