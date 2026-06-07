---
title: An introduction to Spring Data(Reactive)
parent: Rective Data Operations
nav_order: 1
---

# An introduction to Spring Data (Reactive)

Spring Data projects provide first-class reactive support for many data stores, especially NoSQL databases such as Redis, MongoDB, Couchbase and Cassandra.

Blocking APIs such as JDBC and JPA are not suitable for a reactive WebFlux application. To support relational databases reactively, the R2DBC specification and drivers provide non-blocking access via `r2dbc-spi`. See https://r2dbc.io/ for drivers and documentation.

## Reactive Repository

Spring Data offers reactive repository variants such as `ReactiveCrudRepository` and `ReactiveSortingRepository`. Subprojects expose store-specific interfaces, for example `ReactiveMongoRepository` for MongoDB.

```java
@NoRepositoryBean
public interface ReactiveMongoRepository<T,ID>
  extends ReactiveSortingRepository<T,ID>, ReactiveQueryByExampleExecutor<T> { ... }
```

Create repositories by extending the appropriate reactive interface:

```java
interface PostRepository extends ReactiveCrudRepository<Post, Long> {}
interface PostRepository extends ReactiveSortingRepository<Post, Long> {}
interface PostRepository extends ReactiveMongoRepository<Post, Long> {}
```

## Alternative reactive APIs

Spring Data provides adapters to interoperate with other reactive libraries. RxJava 3 and SmallRye Mutiny can be used via adapters, while older RxJava 2 support is deprecated; prefer Reactor or RxJava 3 for new projects.
