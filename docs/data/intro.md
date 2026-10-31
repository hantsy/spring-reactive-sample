
# An introduction to Spring Data (Reactive)

This chapter covers reactive data access in the Spring ecosystem. Every major Spring Data store module ships a reactive variant, and Spring Data R2DBC provides non-blocking access to relational databases. All of them return `Mono` and `Flux`, so they compose naturally with WebFlux.

## What this chapter covers

- The reactive repository pattern and the available store integrations (MongoDB, Redis, Cassandra, Couchbase, Elasticsearch, Neo4j)
- R2DBC for reactive relational access
- Working directly with the store's native reactive drivers in Spring Boot 4
- Mapping, pagination, auditing, and query derivation in reactive repositories

## What you'll learn

- When to use a reactive repository, a reactive template, or the raw driver
- How to model data and perform common operations without blocking
- Practical configuration for each store, with runnable samples

## Quick notes

- Traditional JDBC and JPA are blocking and should be avoided in critical reactive paths. R2DBC provides the reactive relational API; check driver support and maturity for your database.
- Reactive repositories return `Flux` instead of `List` and `Mono` instead of a plain entity. There is no `Page` result in the reactive API — use `Pageable` parameters plus a separate `count` query, or Reactor's `skip`/`take` operators.

## Choosing an access style

| Style | API | Use when |
| :--- | :--- | :--- |
| Reactive repository | `ReactiveMongoRepository`, `R2dbcRepository`, ... | Standard CRUD and derived queries |
| Reactive template/operations | `ReactiveMongoTemplate`, `ReactiveRedisOperations`, `ReactiveNeo4jOperations`, ... | Programmatic queries, dynamic criteria |
| Reactive database client | `DatabaseClient` (R2DBC), `ReactiveNeo4jClient` | Hand-written SQL/Cypher with full control |
| Native driver | `MongoClient` (reactive streams), `CqlSession.executeReactive`, Couchbase `ReactiveCollection`, ... | Performance-sensitive, store-specific operations — see [Database Client Modules](client.md) |

## Chapter roadmap

1. [Database Client Modules](client.md) — Spring Boot 4 lightweight starters and the native reactive drivers behind them
2. [Spring Data Mongo](data-mongo.md) — repositories, templates, pagination, auditing, QueryDSL, tailable queries
3. [Spring Data Redis](data-redis.md) — `ReactiveRedisConnectionFactory`, `ReactiveRedisTemplate`, and Redis pub/sub messaging
4. [Spring Data Cassandra](data-cassandra.md), [Couchbase](data-couchbase.md), [Elasticsearch](data-elasticsearch.md), [Neo4j](data-neo4j.md) — the other NoSQL stores
5. [Spring Data R2DBC](data-r2dbc.md) — reactive relational access
