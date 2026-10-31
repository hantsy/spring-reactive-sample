
# Spring Data R2DBC

R2DBC (Reactive Relational Database Connectivity) brings non-blocking access to relational databases. Spring provides two layers on top of it: the core `spring-r2dbc` module (a `ConnectionFactory`, `DatabaseClient`, and `R2dbcTransactionManager`) and Spring Data R2DBC (`R2dbcEntityTemplate`, `R2dbcRepository`, auditing, entity callbacks).

Like every store in this chapter, this page covers both styles — the **thin client** (`DatabaseClient`, raw `ConnectionFactory`) and the **rich Spring Data** abstractions (`R2dbcEntityTemplate`, `R2dbcRepository`).

!!! note "Dedicated repository"

    The companion repository [hantsy/spring-r2dbc-sample](https://github.com/hantsy/spring-r2dbc-sample) tracks the current R2DBC features in depth — DatabaseClient, `R2dbcEntityTemplate`, repositories, auditing, Postgres JSON/`NOTIFY`, binary data, jOOQ, Kotlin coroutines, and more. The former samples of this repository were updated from the [legacy folder](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy) into the small per-feature modules listed below.

## Connection factories

Every R2DBC application starts with a `ConnectionFactory`. The R2DBC spec defines a portable way to obtain one — from a URL, from options, or from the driver's native builder:

```java
public class PgConnectionFactories {

    static ConnectionFactory fromUrl() {
        return ConnectionFactories.get("r2dbc:postgres://user:password@localhost/test");
    }

    static ConnectionFactory fromOptions() {
        var options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.DATABASE, "test")
                .option(ConnectionFactoryOptions.USER, "user")
                .option(ConnectionFactoryOptions.PASSWORD, "password")
                .option(ConnectionFactoryOptions.DRIVER, "postgresql")
                .build();
        return ConnectionFactories.get(options);
    }

    static ConnectionFactory pgConnectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host("localhost")
                        .database("blogdb")
                        .username("user")
                        .password("password")
                        .build()
        );
    }
}
```

The same three patterns work for every supported driver — H2, MySQL, MariaDB, Oracle, MSSQL — see the [r2dbc-connection-factories](https://github.com/hantsy/spring-reactive-sample/tree/master/r2dbc-connection-factories) sample and the [connection-factories](https://github.com/hantsy/spring-r2dbc-sample/tree/master/connection-factories) module of the dedicated repository for the complete variants.

With Spring Boot, no configuration class is needed at all — set the connection in `application.properties`:

```properties
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/blogdb
spring.r2dbc.username=user
spring.r2dbc.password=password
```

## Schema initialization

R2DBC has no schema generation. Pick one of the three approaches used across the samples:

1. **`ConnectionFactoryInitializer`** (plain Spring applications) — run `schema.sql`/`data.sql` at startup:

    ```java
    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("data.sql")));
        initializer.setDatabasePopulator(populator);

        return initializer;
    }
    ```

2. **Spring Boot SQL init** — with `spring.sql.init.mode=always`, Boot runs the same `schema.sql`/`data.sql` automatically.

3. **[R2dbc Migrate](https://github.com/nkonev/r2dbc-migrate)** — versioned migrations (Flyway-style) for reactive databases:

    ```xml
    <dependency>
      <groupId>name.nkonev.r2dbc-migrate</groupId>
      <artifactId>r2dbc-migrate-spring-boot-starter</artifactId>
      <version>4.0.1</version>
    </dependency>
    ```

    ```properties
    r2dbc.migrate.resources-path=classpath:/db/migration/*.sql
    ```

    Migrations live in `src/main/resources/db/migration/` as `V1__schema.sql`, `V2__sample_data.sql`, and so on — see the [r2dbc-migrate](https://github.com/hantsy/spring-reactive-sample/tree/master/r2dbc-migrate) sample.

## DatabaseClient (thin client)

`DatabaseClient` is the fluent, non-blocking SQL client — it moved into Spring core (`spring-r2dbc`) in 5.3 and is autoconfigured by `spring-boot-starter-r2dbc`. For a plain Spring application, declare it explicitly:

```java
@Bean
DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
    return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .namedParameters(true)
            .build();
}

@Bean
ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
    return new R2dbcTransactionManager(connectionFactory);
}
```

Queries use named parameters and a row mapping function:

```java
public Flux<Post> findByTitleContains(String name) {
    return this.databaseClient
            .sql("SELECT * FROM posts WHERE title LIKE :title")
            .bind("title", "%" + name + "%")
            .map(MAPPING_FUNCTION)
            .all();
}

public Mono<UUID> save(Post p) {
    return this.databaseClient.sql(
                    "INSERT INTO posts (title, content, status, tags) VALUES (:title, :content, :status, :tags)")
            .filter((statement, _) -> statement.returnGeneratedValues("id").execute())
            .bindValues(
                    Map.of("title", p.title(),
                            "content", p.content(),
                            "status", p.status(),
                            "tags", p.tags().toArray(new String[0]))
            )
            .fetch()
            .first()
            .map(r -> (UUID) r.get("id"));
}
```

It also exposes lower-level escapes when needed — statement tuning through the `filter` hook:

```java
public Flux<Post> findAll() {
    return this.databaseClient
            .sql("SELECT * FROM posts")
            .filter((statement, executeFunction) -> statement.fetchSize(10).execute())
            .map(MAPPING_FUNCTION)
            .all();
}
```

See the [r2dbc-databaseclient](https://github.com/hantsy/spring-reactive-sample/tree/master/r2dbc-databaseclient) sample for the complete CRUD repository built on `DatabaseClient` — including generated values, batch inserts through `inConnectionMany(...)`, and transactions with `TransactionalOperator`.

## R2dbcEntityTemplate

`R2dbcEntityTemplate` adds entity mapping, criteria queries, and optimistic locking on top of `DatabaseClient`:

```java
@Component
@RequiredArgsConstructor
public class PostRepository {

    private final R2dbcEntityTemplate template;

    public Flux<Post> findByTitleContains(String name) {
        return this.template.select(Post.class)
                .matching(Query.query(where("title").like("%" + name + "%")).limit(10).offset(0))
                .all();
    }

    public Mono<Post> findById(UUID id) {
        return this.template.selectOne(Query.query(where("id").is(id)), Post.class);
    }

    public Mono<UUID> save(Post p) {
        return this.template.insert(Post.class)
                .using(p)
                .map(Post::id);
    }

    public Mono<Long> update(Post p) {
        return this.template.update(
                Query.query(where("id").is(p.id())),
                Update.update("title", p.title())
                        .set("content", p.content())
                        .set("status", p.status())
                        .set("tags", p.tags()),
                Post.class);
    }

    public Mono<Long> deleteById(UUID id) {
        return this.template.delete(Query.query(where("id").is(id)), Post.class);
    }
}
```

Entities map columns explicitly, and `@Version` enables optimistic locking:

```java
@Table(value = "posts")
record Post(

        @Id
        @Column("id")
        UUID id,

        @Column("title")
        String title,

        @Column("content")
        String content,

        @Column("status")
        Status status,

        @Column("tags")
        List<String> tags,

        @Column("version")
        @Version
        Long version
) {
    enum Status {
        DRAFT, PENDING_MODERATION, PUBLISHED;
    }
}
```

For a plain Spring application, extend `AbstractR2dbcConfiguration` to register the template infrastructure:

```java
@Configuration
@EnableTransactionManagement
@EnableR2dbcAuditing
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host("localhost")
                        .port(5432)
                        .database("blogdb")
                        .username("user")
                        .password("password")
                        .build()
        );
    }

    @Bean
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
```

See the [r2dbc-entitytemplate](https://github.com/hantsy/spring-reactive-sample/tree/master/r2dbc-entitytemplate) sample.

## R2dbcRepository

The repository abstraction derives queries from method names, exactly like the other Spring Data modules:

```java
public interface PostRepository extends R2dbcRepository<Post, UUID> {

    Flux<Post> findByTitleContains(String name);

    Mono<Long> countByTitleContains(String name);
}
```

Activate it with `@EnableR2dbcRepositories` on the configuration class (Spring Boot auto-configures it from the starter):

```java
@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories
@EnableR2dbcAuditing
public class DatabaseConfig extends AbstractR2dbcConfiguration {
    // connectionFactory, transactionManager, initializer as above
}
```

Custom SQL goes through `@Query`:

```java
interface PostRepository extends R2dbcRepository<Post, UUID> {

    @Query("SELECT * FROM posts where title like :title")
    Flux<Post> findByTitleContains(String title);
}
```

See the [r2dbc-repository](https://github.com/hantsy/spring-reactive-sample/tree/master/r2dbc-repository) sample.

## Auditing

Enable auditing with `@EnableR2dbcAuditing` and annotate the entity fields:

```java
@Configuration
@EnableR2dbcAuditing
class DataConfig {

    @Bean
    ReactiveAuditorAware<String> auditorAware() {
        return () -> Mono.just("test");
    }
}
```

```java
@Table(value = "posts")
record Post(
        // ...

        @Column("created_at")
        @CreatedDate
        LocalDateTime createdAt,

        @Column("created_by")
        @CreatedBy
        String createdBy,

        @Column("updated_at")
        @LastModifiedDate
        LocalDateTime updatedAt,

        @Column("updated_by")
        @LastModifiedBy
        String updatedBy,

        @Column("version")
        @Version
        Long version
) {
}
```

In a real application, resolve the auditor from Spring Security's reactive context instead of a hard-coded value. See the [r2dbc-auditing](https://github.com/hantsy/spring-reactive-sample/tree/master/r2dbc-auditing) sample.

## jOOQ for complex queries

When derived queries are not enough, [jOOQ](https://www.jooq.org/) composes type-safe SQL — including advanced constructs such as `MULTISET` aggregations — over the same R2DBC connection:

```java
@Configuration
class JooqConfig {

    @Bean
    DSLContext dslContext(ConnectionFactory connectionFactory) {
        return DSL.using(
                new TransactionAwareConnectionFactoryProxy(connectionFactory),
                SQLDialect.POSTGRES
        );
    }
}
```

```java
@Service
@RequiredArgsConstructor
class PostService {
    private final DSLContext dslContext;

    public Flux<PostSummary> findAll() {
        var p = POSTS;
        var pt = POSTS_TAGS;
        var t = HASH_TAGS;
        var c = COMMENTS;
        var sql = dslContext.select(
                        p.ID,
                        p.TITLE,
                        DSL.field("count(comments.id)", SQLDataType.BIGINT),
                        multiset(select(t.NAME)
                                .from(t)
                                .join(pt).on(t.ID.eq(pt.TAG_ID))
                                .where(pt.POST_ID.eq(p.ID))
                        ).as("tags")
                )
                .from(p.leftJoin(c).on(c.POST_ID.eq(p.ID)))
                .groupBy(p.ID)
                .orderBy(p.CREATED_AT);
        return Flux.from(sql)
                .map(r -> new PostSummary(r.value1(), r.value2(), r.value3(), r.value4().map(Record1::value1)));
    }
}
```

The jOOQ metamodel (`POSTS`, `COMMENTS`, ...) is generated from `schema.sql` by the `jooq-codegen-maven` plugin with a `DDLDatabase`, so the SQL schema stays the single source of truth. See the [r2dbc-jooq](https://github.com/hantsy/spring-reactive-sample/tree/master/r2dbc-jooq) sample and the [jOOQ guide](https://github.com/hantsy/spring-r2dbc-sample/blob/master/docs/jooq.md) in the dedicated repository.

## Testing with Testcontainers

The samples run integration tests against a PostgreSQL Testcontainer — plain Spring modules map properties through an `ApplicationContextInitializer`, Boot modules use `@ServiceConnection`:

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
    }
}

@DataR2dbcTest
@Import(TestcontainersConfiguration.class)
class PostRepositoryTest {
    // posts.findAll() etc. asserted with StepVerifier
}
```

To run any sample application itself, start a PostgreSQL server with Docker:

```bash
docker-compose up postgres
```

## Legacy samples and further resources

- [hantsy/spring-r2dbc-sample](https://github.com/hantsy/spring-r2dbc-sample) — the dedicated repository, with docs for [DatabaseClient](https://github.com/hantsy/spring-r2dbc-sample/blob/master/docs/database-client.md), [Spring Data R2DBC](https://github.com/hantsy/spring-r2dbc-sample/blob/master/docs/data-r2dbc.md), [auditing](https://github.com/hantsy/spring-r2dbc-sample/blob/master/docs/auditing.md), [Postgres JSON/Enum/Notifier](https://github.com/hantsy/spring-r2dbc-sample/blob/master/docs/pg.md), [binary data](https://github.com/hantsy/spring-r2dbc-sample/blob/master/docs/persist-bin.md), and [jOOQ](https://github.com/hantsy/spring-r2dbc-sample/blob/master/docs/jooq.md)
- The former samples of this repository — [legacy/data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/data-r2dbc) and the related `legacy/boot-data-r2dbc-*` modules — are kept for historical reference; the per-feature root modules above supersede them
