# Database Client Modules

In Spring Boot 4, the monolithic autoconfiguration has been modularized. Instead of importing full-featured Spring Data repositories, you can pull in lightweight feature-specific starters to work directly with database drivers or client SDKs. This page is the overview; each store's guide — [Mongo](mongo.md), [Redis](redis.md), [Cassandra](cassandra.md), [Couchbase](couchbase.md), [Elasticsearch](elasticsearch.md), [Neo4j](neo4j.md), [R2DBC](r2dbc.md) — combines the thin native-client approach with the rich Spring Data abstractions (`XXXRepository` and `XXXTemplate`) on one page.

## Spring Boot 4 modularization in a nutshell

One breaking change in Spring Boot 4 is that the single monolithic `spring-boot-autoconfigure` module is split into smaller, fine-grained feature- and library-specific modules. Each feature now provides its own `starter` and `starter-test` modules, which let you include only the autoconfiguration classes and third-party libraries your application requires:

```xml
<!-- Spring Boot 3: monolithic test starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Boot 4: feature-specific starters and dedicated test starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>
```

Autoconfiguration classes are distributed across the feature modules, and the package hierarchy is reorganized around features — for example, `...autoconfigure.data.jpa` is now `...data.jpa.autoconfigure`. For a complete list of Spring Boot 4 modules, see the [Module dependencies section of the Spring Boot 4 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide#module-dependencies).

## Spring Data Client Modules

Spring Boot 4 also modularizes Spring Data dependencies by moving database drivers and client SDKs into dedicated modules:

- JDBC `JdbcClient` and R2DBC `DatabaseClient` are exposed by `spring-boot-starter-jdbc` and `spring-boot-starter-r2dbc`.
- The MongoDB Java client (synchronous or reactive `MongoClient`) is exposed by `spring-boot-starter-mongodb`.

This makes it possible to depend on the specific client libraries you need without pulling in full Spring Data starter sets.

## Choosing thin client vs rich Spring Data

Each store guide in this chapter shows both styles side by side:

| Style | API | Best for |
| :--- | :--- | :--- |
| **Thin client** | The store's native SDK, autoconfigured by a lightweight starter | Targeted, performance-sensitive operations; maximum control, minimum overhead |
| **Rich Spring Data** | `ReactiveMongoRepository`, `ReactiveRedisTemplate`, `R2dbcRepository`, ... | Query derivation, pagination, auditing, object mapping |

The most important rule of thumb: once your application needs query composition, pagination, auditing, or object mapping, reach for the full Spring Data repository abstraction. The native driver approach shines for targeted operations where you want direct control of the driver.

## Summary of the lightweight starters

| Database | Starter | Autoconfigured Bean | Reactive Pattern |
|---|---|---|---|
| Cassandra | `spring-boot-starter-cassandra` | `CqlSession` | `executeReactive()` → `Mono.from(ReactiveResultSet)` — see [Cassandra](cassandra.md) |
| Couchbase | `spring-boot-starter-couchbase` | `Cluster` | `collection.reactive()` → `ReactiveCollection` — see [Couchbase](couchbase.md) |
| Elasticsearch | `spring-boot-starter-data-elasticsearch` | `ReactiveElasticsearchClient` | Spring Data wrapper (no official reactive SDK) — see [Elasticsearch](elasticsearch.md) |
| MongoDB | `spring-boot-starter-mongodb` | `MongoClient` (reactive streams) | `Mono.from(...)` / `Flux.from(...)` — see [Mongo](mongo.md) |
| Neo4j | `spring-boot-starter-neo4j` | `Driver` | `ReactiveSession` + `Mono.usingWhen(...)` — see [Neo4j](neo4j.md) |
| R2DBC | `spring-boot-starter-r2dbc` | `DatabaseClient` | Hand-written SQL — see [R2DBC](r2dbc.md) |
| Redis | _(no dedicated client starter)_ | via `spring-boot-starter-data-redis-reactive` | `ReactiveRedisTemplate` / Lettuce reactive commands — see [Redis](redis.md) |

!!! tip

    Across all stores, `StepVerifier` from the `reactor-test` module is your best friend for asserting reactive sequences in integration tests. Pair it with `@ServiceConnection`-backed Testcontainers for a hermetic, repeatable test environment without any manual port configuration.

For a deeper walkthrough of every client module — including the `RestClient`/`WebClient` web modules and per-store repository code — see the [Spring Boot 4 Modularization](https://github.com/hantsy/spring7-sandbox/blob/master/docs/modularization.md) guide in the companion [spring7-sandbox](https://github.com/hantsy/spring7-sandbox) repository.
