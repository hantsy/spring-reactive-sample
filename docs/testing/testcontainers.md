
# Data Operations with Testcontainers

Repository tests need a real database. [Testcontainers](https://testcontainers.com/) starts the store in a Docker container for the duration of the test and tears it down afterwards. The samples use two wiring styles — one for plain Spring applications and one for Spring Boot — plus Boot's automatic `@ServiceConnection` variant.

!!! tip

    Pair Testcontainers with `StepVerifier` (see [Testing](intro.md)) to assert the reactive query results.

## Plain Spring: ApplicationContextInitializer

The non-Boot samples (cache, the [r2dbc-*](../data/r2dbc.md) modules) implement an `ApplicationContextInitializer` that starts the container, maps its properties into the environment, and stops it when the context closes:

```java
@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18.4-alpine"))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql");

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        context.addApplicationListener((ContextClosedEvent event) -> POSTGRES.stop());
        TestPropertyValues.of(
                "r2dbc.host=" + POSTGRES.getHost(),
                "r2dbc.port=" + POSTGRES.getFirstMappedPort(),
                "r2dbc.username=" + POSTGRES.getUsername(),
                "r2dbc.password=" + POSTGRES.getPassword(),
                "r2dbc.database=" + POSTGRES.getDatabaseName()
        ).applyTo(context.getEnvironment());
    }
}
```

The initializer is applied through `@ContextConfiguration`, so it is reusable across test classes:

```java
@ContextConfiguration(initializers = { ContainersConfiguration.class })
@SpringJUnitConfig(classes = { PostRepositoryTest.TestConfig.class })
public class PostRepositoryTest {

    @Autowired
    PostRepository posts;

    @Test
    public void testSaveAll() {
        var post = posts.save(Post.of("test", "content")).block();
        assertThat(posts.findById(post.id()).block()).isNotNull();
    }
}
```

Because the properties use the application's own keys (`r2dbc.*` in the samples), no container library knowledge leaks into the configuration classes — see the [cache](https://github.com/hantsy/spring-reactive-sample/tree/master/cache) sample for the complete pattern, and the [r2dbc-connection-factories](https://github.com/hantsy/spring-reactive-sample/tree/master/r2dbc-connection-factories) tests for raw-driver variants.

## Spring Boot with @DynamicPropertySource

For Boot applications, the `@Testcontainers` + `@Container` + `@DynamicPropertySource` combination starts the container with the JUnit lifecycle and maps its runtime values into arbitrary property keys. From the [boot-data-mongo-template](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-template) sample:

```java
@DataMongoTest
@Import({PostRepository.class, TestcontainersConfiguration.class})
public class PostRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    PostRepository postRepository;
}
```

Notes:

- `@Testcontainers` (class level) enables the JUnit extension; a static `@Container` field starts before all tests and stops after them.
- The `@DynamicPropertySource` method receives a `DynamicPropertyRegistry` — register the connection properties as suppliers, resolved lazily once the container is up.
- Any property key works here: `spring.data.mongodb.uri`, `spring.r2dbc.url`, `spring.neo4j.uri`, or custom keys.

## Spring Boot with @ServiceConnection

Boot 3.1+ removes even the property mapping: a container bean annotated with `@ServiceConnection` contributes the connection properties automatically — Boot recognizes the container type and registers the right properties. From the [boot-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo) sample:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    }
}

@DataMongoTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public class PostRepositoryTest {

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    PostRepository postRepository;
}
```

No `@DynamicPropertySource`, no property names — Boot wires `spring.data.mongodb.uri` (or `spring.r2dbc.url` for a `PostgreSQLContainer`, `spring.neo4j.uri` for a `Neo4jContainer`, and so on) from the container itself. The same pattern appears with `CassandraContainer`, `CouchbaseContainer`, `ElasticsearchContainer`, and `Neo4jContainer` across the Boot data samples — see the [Data Operations](../data/intro.md) chapter pages for each store's snippet.

## Choosing a style

| Style | Use when |
| :--- | :--- |
| `ApplicationContextInitializer` + `TestPropertyValues` | Plain Spring (no Boot) tests; reusable initializer shared by many test classes |
| `@Testcontainers` + `@DynamicPropertySource` | Boot tests needing explicit control of property keys or container options |
| `@ServiceConnection` | Boot tests with a supported container type — the least code, zero property names |
