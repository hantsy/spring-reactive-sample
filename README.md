# Spring Reactive Sample

This is a sandbox project  for demonstrating  [Reactive Streams](https://www.reactive-streams.org/) support in Spring framework and Spring ecosystem. 

*I've also maintained a series of repos related to ReativeStreams and the latest Spring 5*.

* [Spring RSocket Sample](https://github.com/hantsy/rsocket-sample)
* [Spring Kotlin Coroutines Example](https://github.com/hantsy/spring-kotlin-coroutines-sample)
* [Spring Kotlin DSL/Spring Fu Sample](https://github.com/hantsy/spring-kotlin-dsl-sample)
* [Spring WebMvc Functional Example](https://github.com/hantsy/spring-webmvc-functional-sample)
* [Angular and Spring Reactive Example](https://github.com/hantsy/angular-spring-reactive-sample)
* [Spring R2dbc Example](https://github.com/hantsy/spring-r2dbc-sample)(updates for *Spring 5.3, Spring Data R2dbc 1.2 and Spring Boot 2.4*)

>The source codes are updated to **Spring 6/Spring Boot 3.0**, the Spring Boot 2.7.x based codes are available in [a compressed archive](https://github.com/hantsy/spring-reactive-sample/archive/refs/tags/v1.0.zip) and tagged with [v1.0](https://github.com/hantsy/spring-reactive-sample/releases/tag/v1.0).

## Docs

Read online: [https://hantsy.github.io/spring-reactive-sample/](https://hantsy.github.io/spring-reactive-sample/)


## Sample Codes

The following table list all sample codes related to the above posts. 

### Spring Samples

| name                                                         | description                                                  |
| :----------------------------------------------------------- | ------------------------------------------------------------ |
| [vanilla](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla) | The initial application, includes basic `spring-webflux` feature, use a main class to start up the application |
| [vanilla-jetty](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla-jetty) | Same as **vanilla**, but use Jetty as target runtime         |
| [vanilla-tomcat](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla-tomcat) | Same as **vanilla**, but use Reactor Netty as target runtime |
| [vanilla-undertow](https://github.com/hantsy/spring-reactive-sample/tree/master/vanilla-undertow) | Same as **vanilla**, but use Undertow as target runtime      |
| [java8](https://github.com/hantsy/spring-reactive-sample/tree/master/java8) | Java 8 `CompletableFuture` and `@Async` example              |
| [java9](https://github.com/hantsy/spring-reactive-sample/tree/master/java9) | Same as **vanilla**, Java 9 Flow API support is not ready in Spring 5.0.0.REALESE, planned in 5.0.1, see issue [SPR-16052](https://jira.spring.io/browse/SPR-16052) and the original [discussion on stackoverflow](https://stackoverflow.com/questions/46597924/spring-5-supports-java-9-flow-apis-in-its-reactive-feature/46605983#46605983) |
| [rxjava3](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava3) | Same as **vanilla**, but use Rxjava3 instead of Reactor, since Spring 5.3.0 |
| [smallrye-mutiny](https://github.com/hantsy/spring-reactive-sample/tree/master/smallrye-mutiny) | Same as **vanilla**, but use [SmallRye Mutiny](https://smallrye.io/smallrye-mutiny) instead of Reactor, since Spring 5.3.10 |
| [war](https://github.com/hantsy/spring-reactive-sample/tree/master/war) | Replace the manual bootstrap class in **vanilla** with Spring `ApplicationInitializer`, it can be packaged as a **war** file to be deployed into an external servlet container. |
| [routes](https://github.com/hantsy/spring-reactive-sample/tree/master/routes) | Use `RouterFunction` instead of controller in **vanilla**    |
| [register-bean](https://github.com/hantsy/spring-reactive-sample/tree/master/register-bean) | Programmatic approach to register all beans in `ApplicatonContext` at the application bootstrap |
| [data-neo4j](https://github.com/hantsy/spring-reactive-sample/tree/master/data-neo4j) | Spring Data Neo4j reactive example                           |
| [data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo) | Spring Data Mongo Reactive example                           |
| [data-mongo-pageable](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo-pageable) | Spring Data Mongo Reactive example with pagination support   |
| [data-mongo-transaction](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo-transaction) | Spring Data Mongo Reactive example with `Transaction` support |
| [data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/data-redis) | Spring Data Redis Reactive example                           |
| [data-redis-message](https://github.com/hantsy/spring-reactive-sample/tree/master/data-redis-message) | Spring Data Redis Reactive Example with `ReactiveRedisMessageListenerContainer` |
| [data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/data-cassandra) | Spring Data Cassandra Reactive example                       |
| [data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/data-couchbase) | Spring Data Couchbase Reactive example                       |
| [security](https://github.com/hantsy/spring-reactive-sample/tree/master/security) | Based on **vanilla**, add  Spring Security Reactive support  |
| [security-form](https://github.com/hantsy/spring-reactive-sample/tree/master/security-form) | Same as security, login form example                         |
| [security-user-properties](https://github.com/hantsy/spring-reactive-sample/tree/master/security-user-properties) | Same as security, but use *users.properties* to store users  |
| [security-method](https://github.com/hantsy/spring-reactive-sample/tree/master/security-method) | Replace URI based configuration with method level constraints |
| [security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo) | Based on **data-mongo** and **security**, replace with dummy users in hard codes with Mongo driven store |
| [multipart](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart) | Multipart request handling and file uploading                |
| [multipart-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart-data-mongo) | Multipart and file uploading, but data in Mongo via Spring Data Mongo Reactive `GridFsTemplate` |
| [mvc-thymeleaf](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-thymeleaf) | Traditional web application, use Thymeleaf  as template engine |
| [mvc-mustache](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-mustache) | Traditional web application, use Mustache as template engine |
| [mvc-freemarker](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-freemarker) | Traditional web application, use freemarker as template engine |
| [sse](https://github.com/hantsy/spring-reactive-sample/tree/master/sse) | Server Send Event  example                                   |
| [websocket](https://github.com/hantsy/spring-reactive-sample/tree/master/websocket) | WebSocket example                                            |
| [web-filter](https://github.com/hantsy/spring-reactive-sample/tree/master/web-filter) | `WebFilter` example                                          |
| [groovy](https://github.com/hantsy/spring-reactive-sample/tree/master/groovy) | Written in groovy                                            |
| [groovy-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/groovy-dsl) | Groovy DSL bean definition example                           |
| [client](https://github.com/hantsy/spring-reactive-sample/tree/master/client) | Example of `WebClient` to shake hands with backend reactive  APIs |
| [kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin) | Written in kotlin                                            |
| [kotlin-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-routes) | Use kotlin functional approach to declare beans and bootstrap the application programmatically |
| [kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/kotlin-dsl) | Kotlin DSL bean definition example                           |
| [session](https://github.com/hantsy/spring-reactive-sample/tree/master/session) | Spring Session Example                                       |
| [session-header](https://github.com/hantsy/spring-reactive-sample/tree/master/session-header) | Spring Session `WebSessionIdResolver` Example                |
| [session-data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/session-data-redis) | Spring Data Redis based `ReactiveSessionRepository` Example  |
| [session-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/session-data-mongo) | Spring Data Mongo based `ReactiveSessionRepository` Example  |
| [exception-handler](https://github.com/hantsy/spring-reactive-sample/tree/master/exception-handler) | Exception Handler Example                                    |
| [integration](https://github.com/hantsy/spring-reactive-sample/tree/master/integration) | Spring Integration Example                                   |
| [integration-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/integration-dsl) | Spring Integration Java 8 DSL Example                        |
| [restdocs](https://github.com/hantsy/spring-reactive-sample/tree/master/restdocs) | Spring RestDocs Example                                      |


### Spring Boot Samples

| name                                                         | description                                                  |
| :----------------------------------------------------------- | ------------------------------------------------------------ |
| [boot-start](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-start) | Spring Boot example, including 3 Maven profiles to switch to Jetty, Tomcat, Undertow as target runtime |
| [boot-start-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-start-routes) | Simple `RouterFunction` example                              |
| [boot-mvc-thymeleaf](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-thymeleaf) | Same as mvc-thymeleaf, but based on Spring Boot              |
| [boot-mvc-mustache](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-mustache) | Same as mvc-mustache, but based on Spring Boot               |
| [boot-mvc-freemarker](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-freemarker) | Same as mvc-freemarker,  but based on Spring Boot            |
| [boot-groovy](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-groovy) | Written in Groovy                                            |
| [boot-kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin) | Written in Kotlin                                            |
| [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) | Kotlin specific `BeanDefinitionDSL` Example                  |
| [boot-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-redis) | Example of using `ReactiveRedisConnection` and `RouterFunction` |
| [boot-data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-redis) | Spring Data Redis Example                                    |
| [boot-data-neo4j](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-neo4j) | Spring Data Neo4j example (Spring Boot 2.4)                  |
| [boot-neo4j](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j) | Spring Data Neo4j using `ReactiveNeo4jOperations` (Spring Boot 2.4) |
| [boot-neo4j-cypher](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j-cypher) | Spring Data Neo4j using `ReacitveNeo4jClient` (Spring Boot 2.4) |
| [boot-data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-cassandra) | Spring Data Cassandra Example                                |
| [boot-data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-couchbase) | Spring Data Couchbase  Example                               |
| [boot-data-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-elasticsearch) | Spring Data ElasticSearch  Example                        |
| [boot-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo) | Spring Data Mongo Example(Repository, Auditing, testcontainers)                                   |
| [boot-data-mongo-querydsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-querydsl) | Spring Data Mongo Example with QueryDSL support         |
| [boot-data-mongo-gridfs](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-gridfs) | Spring Data Mongo Example with Gridfs support             |
| [boot-data-mongo-tailable](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-tailable) | Spring Data Mongo tailable document example             |
| [boot-exception-handler](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-exception-handler) | Global Exception Handler  |

### Legacy Codes
Some example codes are becoming deprecated as time goes by, eg. the [SDN Rx project](https://github.com/neo4j/sdn-rx) which was maintained by the Neo4j team is discontinued now, it is highly recommended to migrate to the official [Spring Data Neo4j](https://github.com/spring-projects/spring-data-neo4j). 

And Spring Data R2dbc 1.2 added a lot of breaking changes, so I created another [Spring R2dbc Sample repository](https://github.com/hantsy/spring-r2dbc-sample) to introduce the new features.      

Spring [removed support of RxJava/RxJava2](https://github.com/spring-projects/spring-framework/issues/27443), and other projects, such as Spring Data will remove RxJava/RxJava2 support soon.

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
* [Spring WebFlux: First Steps ](https://dzone.com/articles/spring-webflux-first-steps)
* [Spring-Reactive Example REST Application ](https://dzone.com/articles/spring-reactive-samples)
* [Spring 5 WebFlux and JDBC: To Block or Not to Block ](https://dzone.com/articles/spring-5-webflux-and-jdbc-to-block-or-not-to-block)
* [Reactive Spring 5 and Application Design Impact](https://dzone.com/articles/reactive-spring-5-and-application-design-impact)
* [From Java To Kotlin - Your Cheat Sheet For Java To Kotlin ](https://github.com/MindorksOpenSource/from-java-to-kotlin)
* [From Java to Kotlin](https://fabiomsr.github.io/from-java-to-kotlin/index.html)
* [Petclinic: Spring 5 reactive version](https://github.com/ssouris/petclinic-spring5-reactive/)
* [Spring Framework 5 Kotlin APIs, the functional way](https://spring.io/blog/2017/08/01/spring-framework-5-kotlin-apis-the-functional-way)
* [Kotlin extensions for MongoOperations and ReactiveMongoOperations ](https://github.com/spring-projects/spring-data-mongodb/commit/2359357977e8734331a78c88e0702f50f3a3c75e)
* [Reactive systems using Reactor](http://musigma.org/java/2016/11/21/reactor.html)
* [Lite Rx API Hands-On with Reactor Core 3 ](https://github.com/reactor/lite-rx-api-hands-on)
* [reactor-kotlin-workshop](https://github.com/eddumelendez/reactor-kotlin-workshop)

## Special Thanks

Specials thanks for Jetbrains's support by contributing an open-source license.

[<img src="./jetbrains.png" height="250px" width="250px"/>](https://www.jetbrains.com/?from=spring-reactive-sample)
