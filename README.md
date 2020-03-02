# Spring Reactive Sample

This is a sandbox project  for demonstrating  [Reactive Streams](https://www.reactive-streams.org/)  support in Spring framework and its ecosystem. 


## Docs

* [Reactive Programming with Spring 5(Deprecated)](./docs/GUIDE.md)
* [Accessing Neo4j with SDN Rx](./docs/data-neo4j-rx.md)
* [Accessing RDBMS with Spring Data R2dbc](./docs/data-r2dbc.md)


## Sample Codes

The following table list all sample codes related to the above posts. 

### Spring Samples

| name            | description                                        |
| :---------------| -------------------------------------------------- |
| vanilla                  | The initial application, includes basic `spring-webflux` feature, use a main class to start up the application |
| vanilla-jetty            | Same as **vanilla**, but use Jetty as target runtime         |
| vanilla-tomcat           | Same as **vanilla**, but use Reactor Netty as target runtime |
| vanilla-undertow         | Same as **vanilla**, but use Undertow as target runtime      |
| java9                    | Same as **vanilla**, Java 9 Flow API support is not ready in Spring 5.0.0.REALESE, planned in 5.0.1, see issue [SPR-16052](https://jira.spring.io/browse/SPR-16052) and the original [discussion on stackoverflow](https://stackoverflow.com/questions/46597924/spring-5-supports-java-9-flow-apis-in-its-reactive-feature/46605983#46605983) |
| rxjava                   | Same as **vanilla**, but use Rxjava instead of Reactor       |
| rxjava-jdbc                   | Accessing database with rxjava-jdbc      |
| rxjava2                  | Same as **vanilla**, but use Rxjava2 instead of Reactor      |
| rxjava2-jdbc                  | Accessing database with rxjava2-jdbc      |
| war                      | Replace the manual bootstrap class in **vanilla** with Spring `ApplicationInitializer`, it can be packaged as a **war** file to be deployed into an external servlet container. |
| routes                   | Use `RouterFunction` instead of controller in **vanilla**    |
| register-bean            | Programmatic approach to register all beans in `ApplicatonContext` at application bootstrap |
| data-mongo               | Spring Data Mongo Reactive demo          |
| data-mongo-pageable| Spring Data Mongo Reactive demo with pagiation support          |
| data-mongo-transaction| Spring Data Mongo Reactive demo with `Transaction` support          |
| data-redis               | Spring Data Redis Reactive demo          |
| data-redis-message               | Spring Data Redis Reactive Example with `ReactiveRedisMessageListenerContainer`|
| data-cassandra           | Spring Data Cassandra Reactive demo      |
| data-couchbase           | Spring Data Couchbase Reactive demo      |
| security                 | Based on **vanilla**, add  Spring Security Reactive support  |
| security-form | Same as **secuirty**, login form example |
| security-user-properties | Same as **secuirty**, but use users.properties to store users |
| security-method          | Replace URI based configuration with method level constraints |
| security-data-mongo      | Based on **data-mongo** and **security**, replace with dummy users in hard codes with Mongo driven store |
| multipart                | Mutipart request handling and file uploading                 |
| multipart-data-mongo     | Multipart and file uploading, but data in Mongo via Spring Data Mongo Reactive `GridFsTemplate` |
| mvc-thymeleaf            | Traditional web application, use Thymeleaf  as template engine |
| mvc-mustache            | Traditional web application, use Mustache as template engine|
| mvc-freemarker           | Traditional web application, use freemarker as template engine|
| sse                      | Server Send Event  example               |
| websocket                | WebSocket example                        |
| web-filter                | `WebFilter` example                     |
| groovy                   | Same features as **boot**, but written in groovy             |
| client                   | Demonstration of `WebClient` to shake hands with backend reactive  APIs |
| kotlin                   | Same features as **boot**, but written in kotlin|
| kotlin-gradle            | Use kotlin functional approach to declare beans and bootstrap the application programatically |
| session                  | Spring Session Example|
| session-header           | Spring Session `WebSessionIdResolver` Example|
| session-data-redis           | Spring Data Reids based `ReactiveSessionRepository` Example|
| session-data-mongo           | Spring Data Mongo based `ReactiveSessionRepository` Example|


### Spring Boot based Samples

| name                     | description                                                  |
| :----------------------- | ------------------------------------------------------------ |
| boot                     | Switch to Spring Boot to get autoconfiguration of Spring WebFlux |
| boot-jetty               | Same as **boot**, but use Jetty as target runtime            |
| boot-tomcat              | Same as **boot**, but use Tomcat as target runtime           |
| boot-undertow            | Same as **boot**, but use Undertow as target runtime         |
| boot-routes              | Use `RouterFunction` instead of the general `Controller` in **boot** |
| boot-freemarker          | Same as **mvc-freemarker**, but based on Spring Boot         |

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