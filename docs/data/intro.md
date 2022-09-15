---
sort: 1
---

# An introduction to Spring Data(Reactive)

Spring Data also added Reactive Streams support into the existing projects.

NoSQL support, such as Redis, MongoDB, Couchbase and Cassandra are the first-class citizen in the WebFlux world.

RDBMS support, such as JDBC and JPA are designated for blocking access will not work under the new WebFlux applications. 

To resolve the problem, Spring team leads a new reactive specification [R2dbc](https://R2dbc.io) to access RDBMS. Similar to the JDBC specification, R2dbc provides a collection of standardized APIs and allow you to implement your own database drivers through `r2dbc-spi`. Currently, MySQL(and MariaDB), MSSQL, Oracle, H2, PostgresSQL have got support.  Check the [R2dbc website](https://r2dbc.io/) for more info. 

## Reactive Repository

Similar to the existing `Repository` and it derived interfaces for blocking cases such as `PagingAndSortingRepository` and `CrudRepository`. Spring Data Commons add variants for reactive cases, such as `ReactiveSortingRepository` and `ReactiveCrudRepository`.

> Note: There is no `Paging` variant for reactive interfaces.

The Spring Data subprojects could have its specific top-level `Repository`, such as Spring Data Mongo provides `ReactiveMongoRepository`.

```java
@NoRepositoryBean
public interface ReactiveMongoRepository<T,ID>
extends ReactiveSortingRepository<T,ID>, ReactiveQueryByExampleExecutor<T>{...}
```

To create  your `Repository` ,  create an interface to extend `ReactiveCrudRepository` or `ReactiveSortingRepository`  or a subproject specific `Repository`.  For example:

```java
interface PostRepository extends ReactiveCrudRepository{}
interface PostRepository extends ReactiveSortingRepository{}
interface PostRepository extends ReactiveMongoRepository{}
```

## RxJava 2 and RxJava 3 Support 

The reactive  `Repository` supports RxJava 2 and 3 variants.

* For RxJava 2, there is a `RxJava2CrudRepository` and `RxJava2SortingRepository`.
* For RxJava 3, there is a `RxJava3CrudRepository` and `RxJava3SortingRepository`. 

To use RxJava 2/3 instead of Reactor, just need to declare your `Repository`to extends  `RxJava3CrudRepository` or `RxJava3SortingRepository`. For example:

```java
interface PostRepository extends RxJava3SortingRepository{}
```
