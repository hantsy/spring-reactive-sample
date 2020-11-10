# Testing Spring Data Neo4j 

Since version 1.4,  Spring Boot provided a new test harness so-called **test slice** to test features easier than previous version,  which included a series of  `AutoConfigureXXX` to allow developers to test desired features in an isolated environment. 

For example,  adding a test scoped H2 dependency into your project and annotating your test class with `@DataJpaTest`, you can test your `Repository` class against an embedded H2 instead of the real runtime database. With the `@DataJpaTest`, Spring test context only loads the essential configuration for testing JPA facilities, no need to load the all configuration for the whole application .  

For Spring Data Neo4j, Spring Boot also provides a `@DataNeo4jTest` for testing Neo4j facilities, but unfortunately it does not include an utility to start up an embedded Neoj4 database for your tests. There are some solutions to overcome this barrier.

* Neo4j provides a test harness which provides APIs to start and stop an embedded Neo4j server by programmatic approach.
*  [Testcontainers](https://www.testcontainers.org)  is a generic solution to run Docker containers for the testing framework, it is easy to start a Neo4j database in a Docker container when testing Spring Data Neo4j repositories.

### Test with Neo4j test harness

Add the following dependency into your *pom.xml*.

```xml
<dependency>
    <groupId>org.neo4j.test</groupId>
    <artifactId>neo4j-harness</artifactId>
    <version>${neo4j-harness.version}</version>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-nop</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

In your test, add the following codes to serve a running Neo4j server when running tests.

```java
@DataNeo4jTest
@Transactional(propagation = Propagation.NEVER)
@Slf4j
public class PostRepositoryWithNeo4jHarnessTest {

    private static Neo4j embeddedDatabaseServer;

    @BeforeAll
    static void initializeNeo4j() {

        embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()//disable http server
                .build();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.neo4j.uri", embeddedDatabaseServer::boltURI);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> null);
    }

    @AfterAll
    static void stopNeo4j() {
        embeddedDatabaseServer.close();
    }
    ...
}
```

In the above codes,

* We use the JUnit 5 lifecycle hooks, such as  `beforeAll` and `afterAll` to start and stop an embedded Neo4j server.
* Use a static method annotated with `@DynamicPropertySource` to bind the Neo4j properties to the Spring test context.

Now you can add tests as general.

### Test with Testcontainers

Testcontainers provides a simple programmatic API abstraction for you to bootstrap a Docker container in your testing codes.

Testcontainers is available in the official [Spring initializr](https://start.spring.io). You can add *TestContainers* as dependencies when generating new project using [Spring initializr](https://start.spring.io).

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

And import the testcontainers BOM in the `depedencyManagement`section.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-bom</artifactId>
            <version>${testcontainers.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

In the above code, 

* The `junit-jupiter` artifact is used to integrate *TestContainers* with JUnit 5 platform.
* The `neo4j` artifact provides APIs to compose a Neo4j Docker container.

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
*  By default, JUnit 5 uses a `PER_METHOD` strategy to bootstrap a test, if you set a global ``PER_CLASS` strategy in the *junit-platform.properties*, add a `@TestInstance(TestInstance.Lifecycle.PER_METHOD)` to override it.
* A static method annotated with `@DynamicPropertySource` is used to bind properties from the running Docker container to the Spring environmental variables before the test is running. 
* You can inject `Repository` class, and Neo4j specific `Driver`  beans .etc, in `@DataNeo4jTest` directly.
* Generally, you can add `@BeforeEach`, `@AfterEach` methods to hook the test lifecycle.
* In the `@Test` method, we usually utilizes reactor's `StepVerifier` to assert the result.

> In the original [SDN Rx]((https://github.com/neo4j/sdn-rx/)  ), it provided a `@ReactiveDataNeo4jTest` for testing reactive applications, this annotation is not available in Spring Boot 2.4.

Run the test, it will:

* Preparing Spring test context.
* Check if there is a Docker image existed, if not download it firstly.
* Startup a Docker container for the test.
* Bind the Docker instance properties to Spring environmental variables. 
* Inject Spring resources. 
* Executing test, if there are some hooks, executing hooks before and after the test execution.
* Shutdown the Docker container and clean up Spring test context. 

Alternatively, you can create a `ApplicationContextInitializer` to start a Neo4j Docker container manually.

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

In the above, we use a `ContextConfiguration` to apply the context initializers. In the real world application, you can extract `TestContainerInitializer` to a standalone class, and thus it is easy to reuse in any tests that requires a running Neo4j server instance.

Grab the [source code](http://github.com/hantsy/spring-reactive-sample) from my github.

