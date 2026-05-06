# Spring Reactive Sample

> [!NOTE]
> A huge thanks to [@rajadilipkolli](https://github.com/rajadilipkolli) for his efforts in updating this repository to the newest Spring Boot v4.

This is a sandbox project for demonstrating [Reactive Streams](https://www.reactive-streams.org/) support in the Spring framework and the broader Spring ecosystem.

I've also maintained a series of repositories related to Reactive Streams and the latest Spring 5:

* [Spring RSocket Sample](https://github.com/hantsy/rsocket-sample)
* [Spring Kotlin Coroutines Example](https://github.com/hantsy/spring-kotlin-coroutines-sample)
* [Spring Kotlin DSL/Spring Fu Sample](https://github.com/hantsy/spring-kotlin-dsl-sample)
* [Spring WebMvc Functional Example](https://github.com/hantsy/spring-webmvc-functional-sample)
* [Angular and Spring Reactive Example](https://github.com/hantsy/angular-spring-reactive-sample)
* [Spring R2dbc Example](https://github.com/hantsy/spring-r2dbc-sample) (updates for Spring 5.3, Spring Data R2dbc 1.2, and Spring Boot 2.4)

The source codes have been updated to **Spring 7 / Spring Boot v4**.

* The Spring Boot 3.x based codes are available in a [compressed archive](https://github.com/hantsy/spring-reactive-sample/archive/refs/tags/boot-3.zip) and tagged with [boot-3](https://github.com/hantsy/spring-reactive-sample/releases/tag/boot-3).
* The Spring Boot 2.x based codes are available in a [compressed archive](https://github.com/hantsy/spring-reactive-sample/archive/refs/tags/v1.0.zip) and tagged with [v1.0](https://github.com/hantsy/spring-reactive-sample/releases/tag/v1.0).

## Documentation

Read online: [https://hantsy.github.io/spring-reactive-sample/](https://hantsy.github.io/spring-reactive-sample/)

## Sample Codes

The following table lists all sample codes available in this repository.

### Spring Samples

| Name | Description |
| :--- | :--- |
| [vanilla](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla) | Basic `spring-webflux` features with a main class to start the application |
| [vanilla-reactor-netty2](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla-reactor-netty2) | Same as **vanilla**, but uses Reactor Netty 2.x as the runtime |
| [vanilla-jetty](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla-jetty) | Same as **vanilla**, but uses Jetty as the runtime |
| [vanilla-tomcat](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla-tomcat) | Same as **vanilla**, but uses Tomcat as the runtime |
| [vanilla-undertow](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla-undertow) | Same as **vanilla**, but uses Undertow as the runtime |
| [java8](https://github.com/hantsy/spring-reactive-sample/tree/master/java8) | Java 8 `CompletableFuture` and `@Async` example |
| [java9](https://github.com/hantsy/spring-reactive-sample/tree/master/java9) | Same as **vanilla**, with Java 9 Flow API support (see [SPR-16052](https://jira.spring.io/browse/SPR-16052)) |
| [rxjava3](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava3) | Same as **vanilla**, but uses RxJava 3 instead of Reactor (since Spring 5.3.0) |
| [smallrye-mutiny](https://github.com/hantsy/spring-reactive-sample/tree/master/smallrye-mutiny) | Same as **vanilla**, but uses [SmallRye Mutiny](https://smallrye.io/smallrye-mutiny) instead of Reactor (since Spring 5.3.10) |
| [rxjava3](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava3) | Same as **vanilla**, but use Rxjava3 instead of Reactor, since Spring 5.3.0 |
| [smallrye-mutiny](https://github.com/hantsy/spring-reactive-sample/tree/master/smallrye-mutiny) | Same as **vanilla**, but use [SmallRye Mutiny](https://smallrye.io/smallrye-mutiny) instead of Reactor, since Spring 5.3.10 |
| [war](https://github.com/hantsy/spring-reactive-sample/tree/master/war) | Replaces the manual bootstrap with Spring `ApplicationInitializer`, can be packaged as a **war** file for external servlet containers |
| [routes](https://github.com/hantsy/spring-reactive-sample/tree/master/routes) | Uses `RouterFunction` instead of `@Controller` |
| [register-bean](https://github.com/hantsy/spring-reactive-sample/tree/master/register-bean) | Programmatic bean registration in `ApplicationContext` at bootstrap |
| [data-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/data-elasticsearch) | Spring Data ElasticSearch Reactive example |
| [data-neo4j](https://github.com/hantsy/spring-reactive-sample/tree/master/data-neo4j) | Spring Data Neo4j reactive example |
| [data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo) | Spring Data Mongo Reactive example |
| [data-mongo-pageable](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo-pageable) | Spring Data Mongo Reactive example with pagination support |
| [data-mongo-transaction](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo-transaction) | Spring Data Mongo Reactive example with `Transaction` support |
| [data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/data-redis) | Spring Data Redis Reactive example |
| [data-redis-message](https://github.com/hantsy/spring-reactive-sample/tree/master/data-redis-message) | Spring Data Redis Reactive example with `ReactiveRedisMessageListenerContainer` |
| [data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/data-cassandra) | Spring Data Cassandra Reactive example |
| [data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/data-couchbase) | Spring Data Couchbase Reactive example |
| [security](https://github.com/hantsy/spring-reactive-sample/tree/master/security) | Adds Spring Security Reactive support to **vanilla** |
| [security-form](https://github.com/hantsy/spring-reactive-sample/tree/master/security-form) | Same as **security**, with login form example |
| [security-user-properties](https://github.com/hantsy/spring-reactive-sample/tree/master/security-user-properties) | Same as **security**, but stores users in *users.properties* |
| [security-method](https://github.com/hantsy/spring-reactive-sample/tree/master/security-method) | Replaces URI-based configuration with method-level constraints |
| [security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo) | Based on **data-mongo** and **security**, replaces hard-coded users with Mongo-driven store |
| [multipart](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart) | Multipart request handling and file uploading |
| [multipart-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart-data-mongo) | Multipart and file uploading, data stored in Mongo via Spring Data Mongo Reactive `GridFsTemplate` |
| [mvc-thymeleaf](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-thymeleaf) | Traditional web application using Thymeleaf as template engine |
| [mvc-mustache](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-mustache) | Traditional web application using Mustache as template engine |
| [mvc-freemarker](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-freemarker) | Traditional web application using Freemarker as template engine |
| [sse](https://github.com/hantsy/spring-reactive-sample/tree/master/sse) | Server Send Event example |
| [websocket](https://github.com/hantsy/spring-reactive-sample/tree/master/websocket) | WebSocket example |
| [websocket-reactor-netty2](https://github.com/hantsy/spring-reactive-sample/tree/master/websocket-reactor-netty2) | WebSocket example with Reactor Netty 2 |
| [web-filter](https://github.com/hantsy/spring-reactive-sample/tree/master/web-filter) | `WebFilter` example |
| [groovy](https://github.com/hantsy/spring-reactive-sample/tree/master/groovy) | Written in Groovy |
| [groovy-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/groovy-dsl) | Groovy DSL bean definition example |
| [client](https://github.com/hantsy/spring-reactive-sample/tree/master/client) | `WebClient` example for calling backend reactive APIs |
| [client-jetty](https://github.com/hantsy/spring-reactive-sample/tree/master/client-jetty) | `WebClient` with Jetty Reactive HttpClient |
| [client-reactor-netty2](https://github.com/hantsy/spring-reactive-sample/tree/master/client-reactor-netty2) | `WebClient` with Reactor Netty 2.x HttpClient |
| [client-jdk11-httpclient](https://github.com/hantsy/spring-reactive-sample/tree/master/client-jdk11-httpclient) | `WebClient` with JDK 11 HttpClient |
| [client-apache-httpclient5](https://github.com/hantsy/spring-reactive-sample/tree/master/client-apache-httpclient5) | `WebClient` with Apache HttpClient 5 |
| [cache](https://github.com/hantsy/spring-reactive-sample/tree/master/cache) | Cache Async/Reactive support with in-memory `ConcurrentHashMap` |
| [cache-caffeine](https://github.com/hantsy/spring-reactive-sample/tree/master/cache-caffeine) | Cache Async/Reactive support with Caffeine |
| [cache-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/cache-redis) | Cache Async/Reactive support with Redis |
| [event](https://github.com/hantsy/spring-reactive-sample/tree/master/event) | Application Event Reactive example |
| [kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin) | Written in Kotlin |
| [kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-co) | Written in Kotlin Coroutines |
| [kotlin-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-routes) | Kotlin functional approach to declare beans and bootstrap programmatically |
| [kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-dsl) | Kotlin DSL bean definition example |
| [schedule](https://github.com/hantsy/spring-reactive-sample/tree/master/schedule) | Spring Schedule Reactive example |
| [session](https://github.com/hantsy/spring-reactive-sample/tree/master/session) | Spring Session example |
| [session-header](https://github.com/hantsy/spring-reactive-sample/tree/master/session-header) | Spring Session `WebSessionIdResolver` example |
| [session-data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/session-data-redis) | Spring Data Redis based `ReactiveSessionRepository` example |
| [session-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/session-data-mongo) | Spring Data Mongo based `ReactiveSessionRepository` example |
| [exception-handler](https://github.com/hantsy/spring-reactive-sample/tree/master/exception-handler) | Exception Handler example |
| [integration](https://github.com/hantsy/spring-reactive-sample/tree/master/integration) | Spring Integration example |
| [integration-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/integration-dsl) | Spring Integration Java 8 DSL example |
| [restdocs](https://github.com/hantsy/spring-reactive-sample/tree/master/restdocs) | Spring RestDocs example |

### Spring Boot Samples

| Name | Description |
| :--- | :--- |
| [boot-start](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-start) | Spring Boot example with 3 Maven profiles to switch between Jetty, Tomcat, and Undertow |
| [boot-start-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-start-routes) | Simple `RouterFunction` example |
| [boot-mvc-thymeleaf](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-thymeleaf) | Same as mvc-thymeleaf, based on Spring Boot |
| [boot-mvc-mustache](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-mustache) | Same as mvc-mustache, based on Spring Boot |
| [boot-mvc-freemarker](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-freemarker) | Same as mvc-freemarker, based on Spring Boot |
| [boot-groovy](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-groovy) | Written in Groovy |
| [boot-kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin) | Written in Kotlin |
| [boot-kotlin-co](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co) | Written in Kotlin Coroutines |
| [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) | Kotlin-specific `BeanDefinitionDSL` and `RouterFunctionDsl` example |
| [boot-kotlin-co-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-co-dsl) | Kotlin-specific `BeanDefinitionDSL` and `CoRouterFunctionDsl` example |
| [boot-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-redis) | Example using `ReactiveRedisConnection` and `RouterFunction` |
| [boot-data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-redis) | Spring Data Redis example |
| [boot-data-neo4j](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-neo4j) | Spring Data Neo4j example (Spring Boot 2.4) |
| [boot-neo4j](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j) | Spring Data Neo4j using `ReactiveNeo4jOperations` (Spring Boot 2.4) |
| [boot-neo4j-cypher](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j-cypher) | Spring Data Neo4j using `ReactiveNeo4jClient` (Spring Boot 2.4) |
| [boot-data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-cassandra) | Spring Data Cassandra example |
| [boot-data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-couchbase) | Spring Data Couchbase example |
| [boot-data-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-elasticsearch) | Spring Data ElasticSearch example |
| [boot-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo) | Spring Data Mongo example (Repository, Auditing, testcontainers) |
| [boot-data-mongo-querydsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-querydsl) | Spring Data Mongo example with QueryDSL support |
| [boot-data-mongo-gridfs](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-gridfs) | Spring Data Mongo example with GridFS support |
| [boot-data-mongo-tailable](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-tailable) | Spring Data Mongo tailable document example |
| [boot-exception-handler](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-exception-handler) | Global Exception Handler example |
| [boot-pulsar](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-pulsar) | Spring for Pulsar Reactive example |

### Legacy Codes

Some example codes are becoming deprecated over time, e.g., the [SDN Rx project](https://github.com/neo4j/sdn-rx), which was maintained by the Neo4j team, has been discontinued. It is highly recommended to migrate to the official [Spring Data Neo4j](https://github.com/spring-projects/spring-data-neo4j).

Spring Data R2dbc 1.2 introduced many breaking changes, so I created another [Spring R2dbc Sample repository](https://github.com/hantsy/spring-r2dbc-sample) to introduce the new features.

Spring [removed support of RxJava/RxJava2](https://github.com/spring-projects/spring-framework/issues/27443), and other projects, such as Spring Data, will remove RxJava/RxJava2 support soon.

| name                                                         | description                                                  |
| :----------------------------------------------------------- | ------------------------------------------------------------ |
| [data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/data-r2dbc) | Spring Data R2dbc Example. (*Deprecated*, go to [hantsy/spring-r2dbc-sample](https://github.com/hantsy/spring-r2dbc-sample) to update yourself) |
| [data-r2dbc-postgresql](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/data-r2dbc-postgresql) | Spring Data R2dbc Example, but use PostgreSQL instead(*Deprecated*) |
| [boot-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-r2dbc) | Spring Data R2dbc example using `DatabaseClient`(*Deprecated*) |
| [boot-data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-data-r2dbc) | Spring Data R2dbc example(*Deprecated*)                      |
| [boot-data-r2dbc-auditing](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-data-r2dbc-auditing) | `@EnableR2dbcAuditing` example(*Deprecated*)     |
| [boot-data-r2dbc-postgresql](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-data-r2dbc-postgresql) | Same as boot-data-r2dbc, but use PostgresSQL instead(*Deprecated*) |
| [boot-data-r2dbc-mysql](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-data-r2dbc-mysql) | Same as boot-data-r2dbc, but use MySQL instead(*Deprecated*) |
| [boot-data-r2dbc-mssql](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-data-r2dbc-mssql) | Same as boot-data-r2dbc, but use MS SQL instead(*Deprecated*) |
| [boot-neo4j-rx](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-neo4j-rx) | [SDN Rx](https://github.com/neo4j/sdn-rx) Example but use `ReactiveNeo4jClient`(*Deprecated*) |
| [boot-neo4j-rx-cypher](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-neo4j-rx-cypher) | [SDN Rx](https://github.com/neo4j/sdn-rx) Example using Cypher queries(*Deprecated*) |
| [boot-data-neo4j-rx](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-data-neo4j-rx) | [SDN Rx](https://github.com/neo4j/sdn-rx) Example(*Deprecated*) |
| [rxjava](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava) | Same as **vanilla**, but use Rxjava instead of Reactor       |
| [rxjava-jdbc](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava-jdbc) | Accessing database with rxjava-jdbc.  **NOTE: rxjava-jdbc is a wrapper of blocking Jdbc APIs** |
| [rxjava2](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava2) | Same as **vanilla**, but use Rxjava2 instead of Reactor      |
| [rxjava2-jdbc](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava2-jdbc) | Accessing database with rxjava2-jdbc. **NOTE: rxjava2-jdbc is a wrapper of blocking Jdbc APIs** |

## References

* [Reactive Streams](http://www.reactive-streams.org/), official Reactive Streams website
* [Understanding Reactive types](https://spring.io/blog/2016/04/19/understanding-reactive-types), Spring.IO
* [The WebFlux framework](http://docs.spring.io/spring-framework/docs/5.0.x/spring-framework-reference/web.html#web-reactive), Spring Framework Reference Documentation
* [Reactor Core 3.0 becomes a unified Reactive Foundation on Java 8](https://spring.io/blog/2016/03/11/reactor-core-3-0-becomes-a-unified-reactive-foundation-on-java-8), Spring.IO
* [Reactive Spring](https://spring.io/blog/2016/02/09/reactive-spring), Spring.IO
* Three parts of **Notes on Reactive Programming** by Dave Syer:

  * [Notes on Reactive Programming Part I: The Reactive Landscape](https://spring.io/blog/2016/06/07/notes-on-reactive-programming-part-i-the-reactive-landscape)
  * [Notes on Reactive Programming Part II: Writing Some Code](https://spring.io/blog/2016/06/13/notes-on-reactive-programming-part-ii-writing-some-code)
  * [Notes on Reactive Programming Part III: A Simple HTTP Server Application](https://spring.io/blog/2016/07/20/notes-on-reactive-programming-part-iii-a-simple-http-server-application)

* [Reactive Programming in the Netflix API with RxJava](https://medium.com/netflix-techblog/reactive-programming-in-the-netflix-api-with-rxjava-7811c3a1496a)
* [Reactor by Example](https://www.infoq.com/articles/reactor-by-example)
* [New in Spring 5: Functional Web Framework](https://spring.io/blog/2016/09/22/new-in-spring-5-functional-web-framework)
* [Spring WebFlux: First Steps](https://dzone.com/articles/spring-webflux-first-steps)
* [Spring-Reactive Example REST Application](https://dzone.com/articles/spring-reactive-samples)
* [Spring 5 WebFlux and JDBC: To Block or Not to Block](https://dzone.com/articles/spring-5-webflux-and-jdbc-to-block-or-not-to-block)
* [Reactive Spring 5 and Application Design Impact](https://dzone.com/articles/reactive-spring-5-and-application-design-impact)
* [From Java To Kotlin - Your Cheat Sheet For Java To Kotlin](https://github.com/MindorksOpenSource/from-java-to-kotlin)
* [From Java to Kotlin](https://fabiomsr.github.io/from-java-to-kotlin/index.html)
* [Petclinic: Spring 5 reactive version](https://github.com/ssouris/petclinic-spring5-reactive/)
* [Spring Framework 5 Kotlin APIs, the functional way](https://spring.io/blog/2017/08/01/spring-framework-5-kotlin-apis-the-functional-way)
* [Kotlin extensions for MongoOperations and ReactiveMongoOperations](https://github.com/spring-projects/spring-data-mongodb/commit/2359357977e8734331a78c88e0702f50f3a3c75e)
* [Reactive systems using Reactor](http://musigma.org/java/2016/11/21/reactor.html)
* [Lite Rx API Hands-On with Reactor Core 3](https://github.com/reactor/lite-rx-api-hands-on)
* [reactor-kotlin-workshop](https://github.com/eddumelendez/reactor-kotlin-workshop)

## Special Thanks

Special thanks to JetBrains for supporting an open-source license.

[<img src="./jetbrains.png" height="250px" width="250px"/>](https://www.jetbrains.com/?from=spring-reactive-sample)
