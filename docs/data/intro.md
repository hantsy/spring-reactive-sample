
# An introduction to Spring Data (Reactive)

This chapter introduces the reactive data story in the Spring ecosystem. Every major Spring Data module includes a reactive option, and Spring Data R2DBC brings non-blocking access to relational databases. The common thread is simple: repositories and templates return `Mono` and `Flux`, so they fit naturally into a WebFlux application.

## What this chapter covers

- the reactive repository pattern across the major Spring Data stores
- MongoDB, Redis, Cassandra, Couchbase, Elasticsearch, and Neo4j in a reactive setup
- Spring Data R2DBC for relational databases
- thin-client access to native reactive drivers and the richer Spring Data abstractions (`XXXRepository`, `XXXTemplate`)
- mapping, pagination, auditing, and query derivation in real-world examples

## A quick rule of thumb

- Use traditional JDBC/JPA only when blocking access is acceptable.
- In a reactive application, favor R2DBC or a reactive database driver.
- Reactive repositories return `Flux` instead of `List` and `Mono` instead of a single entity.
- There is no `Page` object in the reactive API; use `Pageable` parameters plus a separate `count` query, or `skip`/`take` operators.

## Thin client vs. rich Spring Data

There are two common ways to work with a database in a reactive Spring app:

- a thin client approach, where you use the database's native SDK directly
- a rich Spring Data approach, where you leverage Spring Data repositories, templates, and mapping support

The thin client is ideal when you want direct control over the driver and very targeted access patterns. It usually gives you maximum efficiency, but it also means more manual work for query composition, pagination, and object mapping.

The richer Spring Data abstraction is often easier to maintain when the application needs query derivation, auditing, pagination, or mapping support. You spend less time wiring infrastructure and more time modeling the domain.

| Style | API | Best for |
| :--- | :--- | :--- |
| **Thin client** | native driver + starter autoconfiguration | performance-sensitive operations, fine control, low overhead |
| **Rich Spring Data** | `ReactiveMongoRepository`, `ReactiveRedisTemplate`, `R2dbcRepository`, ... | query derivation, pagination, auditing, object mapping |

A good rule: if your app needs anything beyond a simple CRUD flow, Spring Data often saves time and reduces boilerplate.

## Spring Boot 4 starter landscape

Spring Boot 4 modularizes the old monolithic auto-configuration model into more focused starter modules. That makes it easier to include only the database and driver support your application actually needs.

| Database | Starter | Autoconfigured Bean | Reactive Pattern |
|---|---|---|---|
| Cassandra | `spring-boot-starter-cassandra` | `CqlSession` | `executeReactive()` → `Mono.from(ReactiveResultSet)` — see [Cassandra](cassandra.md) |
| Couchbase | `spring-boot-starter-couchbase` | `Cluster` | `collection.reactive()` → `ReactiveCollection` — see [Couchbase](couchbase.md) |
| Elasticsearch | `spring-boot-starter-data-elasticsearch` | `ReactiveElasticsearchClient` | Spring Data wrapper — see [Elasticsearch](elasticsearch.md) |
| MongoDB | `spring-boot-starter-mongodb` | `MongoClient` (reactive streams) | `Mono.from(...)` / `Flux.from(...)` — see [Mongo](mongo.md) |
| Neo4j | `spring-boot-starter-neo4j` | `Driver` | `ReactiveSession` + `Mono.usingWhen(...)` — see [Neo4j](neo4j.md) |
| R2DBC | `spring-boot-starter-r2dbc` | `DatabaseClient` | handwritten SQL — see [R2DBC](r2dbc.md) |
| Redis | via `spring-boot-starter-data-redis-reactive` | reactive Redis connection and templates | `ReactiveRedisTemplate` / Lettuce commands — see [Redis](redis.md) |

!!! tip

    `StepVerifier` from the `reactor-test` module is invaluable for checking reactive flows in tests. Pair it with Testcontainers and `@ServiceConnection` to keep each integration test isolated and deterministic.

See the [Spring Boot 4 Modularization](https://github.com/hantsy/spring7-sandbox/blob/master/docs/modularization.md) guide in the companion [spring7-sandbox](https://github.com/hantsy/spring7-sandbox) repository.

## Chapter roadmap

1. [Accessing MongoDB with Spring Data Mongo](mongo.md) — repositories, templates, pagination, auditing, QueryDSL, tailable queries, and GridFS
2. [Accessing Redis with Spring Data Redis](redis.md) — `ReactiveRedisConnectionFactory`, `ReactiveRedisTemplate`, and pub/sub messaging
3. [Accessing Cassandra with Spring Data Cassandra](cassandra.md), [Couchbase](couchbase.md), [Elasticsearch](elasticsearch.md), [Neo4j](neo4j.md) — the other major NoSQL stores
4. [Accessing RDBMS with Spring R2DBC and Spring Data R2DBC](r2dbc.md) — `DatabaseClient`, `R2dbcEntityTemplate`, repositories, auditing, and jOOQ
