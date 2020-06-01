# Spring Reactive Sample

This is a sandbox project  for examplenstrating  [Reactive Streams](https://www.reactive-streams.org/)  support in Spring framework and its ecosystem. 

Other repos related to ReativeStreams and the latest Spring.

* [Spring RSocket Sample](https://github.com/hantsy/rsocket-sample)
* [Spring Kotlin Coroutines Example](https://github.com/hantsy/spring-kotlin-coroutines-sample)
* [Spring DSL Sample](https://github.com/hantsy/spring-kotlin-dsl-sample)
* [Spring WebMvc Functional Example](https://github.com/hantsy/spring-webmvc-functional-sample)


## Docs

* [Reactive Programming with Spring 5(Deprecated)](./docs/GUIDE.md)
* [Accessing Neo4j with SDN Rx](./docs/data-neo4j-rx.md)
* [Accessing RDBMS with Spring Data R2dbc](./docs/data-r2dbc.md)


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
| [rxjava](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava) | Same as **vanilla**, but use Rxjava instead of Reactor       |
| [rxjava-jdbc](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava-jdbc) | Accessing database with rxjava-jdbc                          |
| [rxjava2](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava2) | Same as **vanilla**, but use Rxjava2 instead of Reactor      |
| [rxjava2-jdbc](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava2-jdbc) | Accessing database with rxjava2-jdbc                         |
| [war](https://github.com/hantsy/spring-reactive-sample/tree/master/war) | Replace the manual bootstrap class in **vanilla** with Spring `ApplicationInitializer`, it can be packaged as a **war** file to be deployed into an external servlet container. |
| [routes](https://github.com/hantsy/spring-reactive-sample/tree/master/routes) | Use `RouterFunction` instead of controller in **vanilla**    |
| [register-bean](https://github.com/hantsy/spring-reactive-sample/tree/master/register-bean) | Programmatic approach to register all beans in `ApplicatonContext` at the application bootstrap |
| [data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo) | Spring Data Mongo Reactive example                           |
| [data-mongo-pageable](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo-pageable) | Spring Data Mongo Reactive example with pagination support   |
| [data-mongo-transaction](https://github.com/hantsy/spring-reactive-sample/tree/master/data-mongo-transaction) | Spring Data Mongo Reactive example with `Transaction` support |
| [data-redis](https://github.com/hantsy/spring-reactive-sample/tree/master/data-redis) | Spring Data Redis Reactive example                           |
| [data-redis-message](https://github.com/hantsy/spring-reactive-sample/tree/master/data-redis-message) | Spring Data Redis Reactive Example with `ReactiveRedisMessageListenerContainer` |
| [data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/data-cassandra) | Spring Data Cassandra Reactive example                       |
| [data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/data-couchbase) | Spring Data Couchbase Reactive example                       |
| [data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/data-r2dbc) | Spring Data R2dbc Example                                    |
| [data-r2dbc-postgresql](https://github.com/hantsy/spring-reactive-sample/tree/master/data-r2dbc-postgresql) | Spring Data R2dbc Example, but use PostgreSQL instead        |
| [security](https://github.com/hantsy/spring-reactive-sample/tree/master/security) | Based on **vanilla**, add  Spring Security Reactive support  |
| [security-form](https://github.com/hantsy/spring-reactive-sample/tree/master/security-form) | Same as security, login form example                         |
| [security-user-properties](https://github.com/hantsy/spring-reactive-sample/tree/master/security-user-properties) | Same as security, but use *users.properties* to store users  |
| [security-method](https://github.com/hantsy/spring-reactive-sample/tree/master/security-method) | Replace URI based configuration with method level constraints |
| [security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo) | Based on **data-mongo** and **security**, replace with dummy users in hard codes with Mongo driven store |
| [multipart](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart) | Mutipart request handling and file uploading                 |
| [multipart-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart-data-mongo) | Multipart and file uploading, but data in Mongo via Spring Data Mongo Reactive `GridFsTemplate` |
| [mvc-thymeleaf](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-thymeleaf) | Traditional web application, use Thymeleaf  as template engine |
| [mvc-mustache](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-mustache) | Traditional web application, use Mustache as template engine |
| [mvc-freemarker](https://github.com/hantsy/spring-reactive-sample/tree/master/mvc-freemarker) | Traditional web application, use freemarker as template engine |
| [sse](https://github.com/hantsy/spring-reactive-sample/tree/master/sse) | Server Send Event  example                                   |
| [websocket](https://github.com/hantsy/spring-reactive-sample/tree/master/websocket) | WebSocket example                                            |
| [web-filter](https://github.com/hantsy/spring-reactive-sample/tree/master/web-filter) | `WebFilter` example                                          |
| [groovy](https://github.com/hantsy/spring-reactive-sample/tree/master/groovy) | Written in groovy                                            |
| [groovy-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/groovy-dsl) | Groovy DSL bean definition example                           |
| [client](https://github.com/hantsy/spring-reactive-sample/tree/master/client) | examplenstration of `WebClient` to shake hands with backend reactive  APIs |
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
| [boot-start](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-start) | Switch to Spring Boot to get autoconfiguration of Spring WebFlux |
| [boot-start-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-start-routes) | Simple `RouterFunction` example                              |
| [boot-jetty](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-jetty) | Example using Jetty as target runtime                        |
| [boot-tomcat](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-tomcat) | Example using Tomcat as target runtime                       |
| [boot-undertow](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-undertow) | Example using Undertow as target runtime                     |
| [boot-routes](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-routes) | `RouterFunction` CRUD Example                                |
| [boot-mvc-thymeleaf](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-thymeleaf) | Same as mvc-thymeleaf, but based on Spring Boot              |
| [boot-mvc-mustache](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-mustache) | Same as mvc-mustache, but based on Spring Boot               |
| [boot-mvc-freemarker](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-mvc-freemarker) | Same as mvc-freemarker,  but based on Spring Boot            |
| [boot-groovy](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-groovy) | Written in Groovy                                            |
| [boot-kotlin](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin) | Written in Kotlin                                            |
| [boot-kotlin-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-kotlin-dsl) | Kotlin specific `BeanDefinitionDSL` Example                  |
| [boot-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-r2dbc) | Spring Data R2dbc example using `DatabaseClient`             |
| [boot-data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-r2dbc) | Spring Data R2dbc example                                    |
| [boot-data-r2dbc-postgresql](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-r2dbc-postgresql) | Same as boot-data-r2dbc, but use PostgresSQL instead         |
| [boot-data-r2dbc-mysql](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-r2dbc-mysql) | Same as boot-data-r2dbc, but use MySQL instead               |
| [boot-data-r2dbc-mssql](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-r2dbc-mssql) | Same as boot-data-r2dbc, but use MS SQL instead              |
| [boot-neo4j-rx](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j-rx) | Spring Data Neo4j Rx Example but use `ReactiveNeo4jClient`.  |
| [boot-neo4j-rx-cypher](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-neo4j-rx-cypher) | Spring Data Neo4j Rx Example using Cypher queries            |
| [boot-data-neo4j-rx](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-neo4j-rx) | Spring Data Neo4j Rx Example                                 |
| [boot-data-cassandra](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-cassandra) | Spring Data Cassandra Example                                |
| [boot-data-couchbase](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-couchbase) | Spring Data Couchbase  Example                               |
| [boot-data-elasticsearch](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-elasticsearch) | Spring Data ElasticSearch  Example                           |
| [boot-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo) | Spring Data Mongo Example                                    |
| [boot-data-mongo-querydsl](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-querydsl) | Spring Data Mongo Example with QueryDSL support              |
| [boot-data-mongo-gridfs](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-gridfs) | Spring Data Mongo Example with Gridfs support                |
| [boot-exception-handler](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-exception-handler) | Global Exception Handler                                     |


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