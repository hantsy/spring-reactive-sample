# An introduciton to Spring WebFlux

 **Reactive** or **Reactive Streams** is a hot topic in these years, you can see it in blog entries, presentations, or some online course.

What is Reactive Streams? 

The following is extracted from the official Reactive Streams website:

>Reactive Streams is an initiative to provide a standard for asynchronous stream processing with non-blocking back pressure.This encompasses efforts aimed at runtime environments (JVM and JavaScript) as well as network protocols.

Currently, the JVM specification is completed, it includes a Java API(four simple interface), a textual Specification, a TCK and implementation examples. Check [Reactive Streams for JVM](https://github.com/reactive-streams/reactive-streams-jvm#reactive-streams) for more details.

Reactor and RxJava2 have implemented this specification, and Java 9 also adopted it in the new Flow API. 

The Spring embraces [Reactive Streams](http://www.reactive-streams.org/) in the new 5.x era. 

For Spring developers, it brings a complete new programming model. 

* Spring added a new `spring-webflux` module in it is core framework, and provided built-in reactive programming support via Reactor and RxJava. 
* Spring Security 5 also added reactive feature. 
* Spring supports RSocket which a new bi-direction messaging protocol.
* In Spring Data umbrella projects, a new `ReactiveSortingRepository` interface is added in Spring Data Commons. Redis, Mongo, Cassandra subprojects firstly got reactive supports. For RDBMS, Spring created R2dbc sepc and R2dbc is part of Spring since 5.3. 
* Spring Session also began to add reactive features, an reactive variant for its `SessionRepository` is included since 2.0.
* Spring Integration added flux message channel and reactive programming APIs.
