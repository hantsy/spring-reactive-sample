
# An introduction to Spring Data (Reactive)

Spring Data is a family of projects that gives different data stores a consistent programming model. At its foundation is **Spring Data Commons**, which defines shared concepts such as repositories, paging and sorting, auditing, and mapping conventions. Individual modules build on those concepts and adapt them to a particular store, such as MongoDB, Redis, Cassandra, or a relational database through R2DBC.

Understanding the common layer makes the store-specific chapters easier to follow. The database changes, but many of the choices you make in the application remain familiar.

## The repository model

The repository hierarchy starts with `Repository<T, ID>`, a marker interface that associates a repository with an entity type and its identifier type. It does not expose operations by itself. More specific interfaces add the capabilities an application needs:

| Interface | Role |
|---|---|
| `CrudRepository<T, ID>` | Synchronous CRUD operations |
| `PagingAndSortingRepository<T, ID>` | Paging and sorting operations |
| `ReactiveCrudRepository<T, ID>` | Reactive CRUD operations |
| `ReactiveSortingRepository<T, ID>` | Reactive sorting operations |

`ReactiveCrudRepository<T, ID>` extends the common `Repository<T, ID>` marker and provides reactive CRUD operations. `ReactiveSortingRepository<T, ID>` adds reactive sorting support. These are separate interfaces rather than one inheritance chain, so a repository can compose the capabilities it needs.

For a reactive store, a repository can extend a store-specific interface such as `ReactiveMongoRepository<Post, String>`. That interface combines common reactive CRUD behavior with the query and mapping support of the store module. The reactive repository APIs use Reactor types: a single result is represented by `Mono<T>`, while a stream of results is represented by `Flux<T>`.

For example:

```java
interface PostRepository extends ReactiveCrudRepository<Post, String> {
    Flux<Post> findByTitleContaining(String term);
}
```

The repository is an abstraction, not a requirement. You can define query methods and let Spring Data implement them, or choose a template or native driver when you need more control. When you need sorting as well as reactive CRUD, compose `ReactiveSortingRepository` with `ReactiveCrudRepository` where the store module supports it. Store-specific repository interfaces may already combine these capabilities, so check the module's API before adding both.

## Repositories, templates, and drivers

Spring Data modules commonly offer several levels of data access:

- **Repositories** provide a focused interface for common operations, derived queries, and—where supported—paging or sorting.
- **Templates**, such as `ReactiveMongoTemplate` or `R2dbcEntityTemplate`, offer a fluent API for operations that need more control over queries, mapping, or updates.
- **Native drivers** expose the database vendor's API directly. They can be useful for specialized features, but leave more integration work to the application.

These approaches can coexist in one application. Use the highest-level API that fits the operation, and reach for a template or driver when the repository abstraction no longer expresses the query clearly.

## Auditing shared across stores

Spring Data Commons also provides auditing support for common entity metadata. Auditing can populate fields such as creation time, last-modified time, creator, and last modifier. The familiar annotations include `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, and `@LastModifiedBy`.

The annotations describe the fields, but auditing must also be enabled for the store module. The configuration varies by module—for example, reactive MongoDB and Neo4j use their reactive auditing enablement annotations. If the application needs to record the current user, provide an auditor provider; reactive modules support `ReactiveAuditorAware` so the auditor can be resolved without blocking.

Auditing is useful for operational metadata, but it does not replace domain-specific history or event logging. It answers questions such as “when was this record last changed?” rather than preserving every change.

## Reactive data access

Reactive Spring Data modules carry the common model into non-blocking applications. Their repository methods return reactive types rather than eagerly materialized collections or individual values. Spring Data R2DBC brings this model to relational databases; other modules integrate with reactive drivers or provide a reactive API of their own.

The rest of this chapter explores those store-specific differences, while the repository, template, and auditing concepts above provide a shared foundation.

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

## A practical reading path

A good way to use this chapter is to move from the most familiar patterns to the more specialized ones:

- start with [Accessing MongoDB with Spring Data Mongo](mongo.md) for a clear, repository-first example that shows the reactive data model in action
- continue with [Accessing Redis with Spring Data Redis](redis.md) to see how a lightweight key-value store fits into a reactive app
- then explore [Accessing Cassandra with Spring Data Cassandra](cassandra.md), [Couchbase](couchbase.md), [Elasticsearch](elasticsearch.md), and [Neo4j](neo4j.md) for the other common NoSQL options
- finish with [Accessing RDBMS with Spring R2DBC and Spring Data R2DBC](r2dbc.md) to see how reactive SQL works in the same Spring ecosystem

If you already know the basics, skip directly to the database that matches your use case. The important idea is consistent: the data access layer stays reactive, but the APIs and conventions vary depending on the store.
