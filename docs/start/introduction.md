---
title: An introduction to Spring WebFlux
sort: 1
---

# An introduction to Spring WebFlux


**Reactive** or **Reactive Streams** is a hot topic in these years, you can see it in blog entries, presentations, or some online course.

## What is Reactive Streams? 

The following is extracted from the official Reactive Streams website:

>Reactive Streams is an initiative to provide a standard for asynchronous stream processing with non-blocking back pressure. This encompasses efforts aimed at runtime environments (JVM and JavaScript) as well as network protocols.

Currently, the JVM specification is completed, it includes a Java API(four simple interfaces), a textual Specification, a TCK and implementation examples. 

There are 4 core comonents provided in ReactiveStreams JVM specification.

*  [Publisher](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html)<T> - A `Publisher` is a provider of a potentially unbounded number of sequenced elements, publishing them according to the demand received from its `Subscriber`(s). 
*    [Subscriber](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscriber.html)<T> - Will receive call to `Subscriber.onSubscribe(Subscription)` once after passing an instance of `Subscriber` to `Publisher.subscribe(Subscriber)`.
*     [Subscription](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscription.html) -  A `Subscription` represents a one-to-one lifecycle of a `Subscriber` subscribing to a `Publisher`. 
*      [Processor](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Processor.html)<T,R> -  A Processor represents a processing stage—which is both a `Subscriber` and a `Publisher` and obeys the contracts of both.

More info, please check  [Reactive Streams for JVM](https://github.com/reactive-streams/reactive-streams-jvm#reactive-streams)  project  and [API docs](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/package-summary.html).

Reactor and RxJava 2/3 have implemented this specification, and Java 9 has already adopted it in the new Flow API. 

## Reactor

The [Reactor project](https://projectreactor.io/) team works close to the Spring Team, and Reactor is the default ReactiveStreams implementation supported in Spring WebFlux.

In Reactor, there are two reactive types implements `Publisher` interface.

* Flux -  represents 0..N items in the sequence.
* Mono - represents 0 or 1 item in the sequence.

## RxJava 2 and RxJava 3

RxJava 2 and 3 APIs are similar, the difference is RxJava 2 targets Java 6 and RxJava 3 is rewritten in Java 8.  RxJava 2 is end of life, and for new projects RxJava 3 is preferred.

In [RxJava](https://github.com/ReactiveX/RxJava), there are some terminologies.

- Observable - emits 1 or more items.
- Flowable - similar to `Obserable`, and provides backpressure capacity.
- Single - emits  exact 1 item .
- Maybe - emits  0 or 1 item.
- Completable - execute some tasks instead of emitting items.

## Flow API in Java 9+

Since Java 9,  Java platform includes a [Flow API](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html) which repackage all interfaces provided  in [Reactive Streams for JVM](https://github.com/reactive-streams/reactive-streams-jvm#reactive-streams)  and an implementation class - [SubmissionPublisher](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/SubmissionPublisher.html) .

Java 11 adds a new reactive Http Client APIs which is based on the Flow APIs and free you from other 3rd party HttpClient libraries, such as Apache HttpClients, OkHttp, etc.

## Spring WebFlux

The Spring embraces [Reactive Streams](http://www.reactive-streams.org/) in the new 5.x era

For Spring developers, it brings a complete new programming model. 

* Spring added a new `spring-webflux` module in it is core framework, and provided built-in reactive programming support via Reactor and RxJava 2/3(RxJava 1 support is removed in the latest Spring 5.3). 
* Spring Security 5 also added reactive feature. 
* Spring supports RSocket which a new bi-direction messaging protocol.
* In Spring Data umbrella projects, a new `ReactiveSortingRepository` interface is added in Spring Data Commons. Redis, Mongo, Cassandra subprojects firstly got reactive supports. For RDBMS, Spring created R2dbc sepc and R2dbc is part of Spring since 5.3. 
* Spring Session also began to add reactive features, an reactive variant for its `SessionRepository` is included since 2.0.
* Spring Integration added flux message channel and reactive programming APIs.
