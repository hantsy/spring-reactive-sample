
# An introduction to Spring Data (Reactive)

This chapter covers reactive data access in the Spring ecosystem. Every major Spring Data store module ships a reactive variant, and Spring Data R2DBC provides non-blocking access to relational databases. All of them return `Mono` and `Flux`, so they compose naturally with WebFlux.

## What this chapter covers

- The reactive repository pattern and the available store integrations (MongoDB, Redis, Cassandra, Couchbase, Elasticsearch, Neo4j)
- R2DBC for reactive relational access
- Working directly with the store's native reactive drivers (the *thin client* style) and with the rich Spring Data abstractions (`XXXRepository`, `XXXTemplate`)
- Mapping, pagination, auditing, and query derivation in reactive repositories

## Quick notes

- Traditional JDBC and JPA are blocking and should be avoided in critical reactive paths. R2DBC provides the reactive relational API; check driver support and maturity for your database.
- Reactive repositories return `Flux` instead of `List` and `Mono` instead of a plain entity. There is no `Page` result in the reactive API — use `Pageable` parameters plus a separate `count` query, or Reactor's `skip`/`take` operators.

## Choosing thin client vs rich Spring Data

Each store guide in this chapter follows the same path: generate a project as in [Getting Started](../start/intro.md), add the dependency, try the store's **native client** first (autoconfigured by the lightweight starter), test it with Testcontainers, and then move to the **rich Spring Data** abstractions (`XXXRepository`, `XXXTemplate`) — with the store-specific configuration of the non-Boot `data-xxx` samples introduced along the way.

| Style | API | Best for |
| :--- | :--- | :--- |
| **Thin client** | The store's native SDK, autoconfigured by a lightweight starter | Targeted, performance-sensitive operations; maximum control, minimum overhead |
| **Rich Spring Data** | `ReactiveMongoRepository`, `ReactiveRedisTemplate`, `R2dbcRepository`, ... | Query derivation, pagination, auditing, object mapping |

The most important rule of thumb: once your application needs query composition, pagination, auditing, or object mapping, reach for the full Spring Data repository abstraction. The native driver approach shines for targeted operations where you want direct control of the driver.

## Summary of the lightweight starters

One breaking change in Spring Boot 4 is that the single monolithic `spring-boot-autoconfigure` module is split into smaller, fine-grained feature- and library-specific modules. Spring Boot 4 also modularizes Spring Data dependencies by moving database drivers and client SDKs into dedicated modules, allowing you to include only what your application actually needs.

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

See the [Spring Boot 4 Modularization](https://github.com/hantsy/spring7-sandbox/blob/master/docs/modularization.md) guide in the companion [spring7-sandbox](https://github.com/hantsy/spring7-sandbox) repository.

## Chapter roadmap

1. [Accessing MongoDB with Spring Data Mongo](mongo.md) — repositories, templates, pagination, auditing, QueryDSL, tailable queries, GridFS
2. [Accessing Redis with Spring Data Redis](redis.md) — `ReactiveRedisConnectionFactory`, `ReactiveRedisTemplate`, and Redis pub/sub messaging
3. [Accessing Cassandra with Spring Data Cassandra](cassandra.md), [Couchbase](couchbase.md), [Elasticsearch](elasticsearch.md), [Neo4j](neo4j.md) — the other NoSQL stores
4. [Accessing RDBMS with Spring R2DBC and Spring Data R2DBC](r2dbc.md) — DatabaseClient, `R2dbcEntityTemplate`, repositories, auditing, jOOQ
