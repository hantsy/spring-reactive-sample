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

There are 4 core components provided in ReactiveStreams JVM specification.

* [`Publisher<T>`](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html) - A `Publisher` is a provider of a potentially unbounded number of sequenced elements, publishing them according to the demand received from its `Subscriber`(s). 
* [`Subscriber<T>`](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscriber.html) - Will receive call to `Subscriber.onSubscribe(Subscription)` once after passing an instance of `Subscriber` to `Publisher.subscribe(Subscriber)`.
* [`Subscription`](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscription.html) -  A `Subscription` represents a one-to-one lifecycle of a `Subscriber` subscribing to a `Publisher`. 
* [`Processor<T,R>`](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Processor.html) -  A Processor represents a processing stage—which is both a `Subscriber` and a `Publisher` and obeys the contracts of both.

More info, please check  [Reactive Streams for JVM](https://github.com/reactive-streams/reactive-streams-jvm#reactive-streams)  project  and [API docs](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/package-summary.html).

Reactor and RxJava 2/3 have implemented this specification, and Java 9 has already adopted it in the new Flow API. 

## Reactor

The [Reactor project](https://projectreactor.io/) team works close to the Spring Team, and Reactor is the default ReactiveStreams implementation supported in Spring WebFlux.

In Reactor, there are two reactive types implements `Publisher` interface.

* [`Flux`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html) -  A  [`Publisher`](https://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html?is-external=true) with rx operators that emits 0 to N elements, and then completes (successfully or with an error). 
* [`Mono`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html) - A  [`Publisher`](https://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html?is-external=true) with basic rx operators that emits at most one item *via* the `onNext` signal then terminates with an `onComplete` signal (successful Mono, with or without value), or only emits a single `onError` signal (failed Mono).  

## RxJava 2 and RxJava 3

RxJava 2 and 3 APIs are similar, the difference is RxJava 2 targets Java 6 and RxJava 3 is rewritten in Java 8.  RxJava 2 is end of life, and for new projects RxJava 3 is preferred.

In [RxJava](https://github.com/ReactiveX/RxJava), there are some terminologies.

- [`Observable`](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html) - The `Observable` class is the non-backpressured, optionally multi-valued base reactive class that offers factory methods, intermediate operators and the ability to consume synchronous and/or asynchronous reactive dataflows. 
- [`Flowable`](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html) - The `Flowable` class that implements the [Reactive Streams](https://github.com/reactive-streams/reactive-streams-jvm) [`Publisher`](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html?is-external=true) Pattern and offers factory methods, intermediate operators and the ability to consume reactive dataflows. `Flowable`s support backpressure and require `Subscriber`s to signal demand via [`Subscription.request(long)`](http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscription.html?is-external=true#request-long-).
- [`Single`](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Single.html) - The `Single` class implements the Reactive Pattern for a single value response.  `Single` behaves similarly to [`Observable`](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html) except that it can only emit either a single successful value or an error (there is no `onComplete` notification as there is for an `Observable`). 
- [`Maybe`](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Maybe.html) - The `Maybe` class represents a deferred computation and emission of a single value, no value at all or an exception. 
- [`Completable`](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Completable.html) - The `Completable` class represents a deferred computation without any value but only indication for completion or exception.  `Completable` behaves similarly to [`Observable`](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html) except that it can only emit either a completion or error signal (there is no `onNext` or `onSuccess` as with the other reactive types). 

## Flow API in Java 9+

Since Java 9,  Java platform adds an [java.util.concurrent.Flow](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html)  interface which repackage all interfaces in [Reactive Streams for JVM](https://github.com/reactive-streams/reactive-streams-jvm#reactive-streams)  and a `Flow.Publisher` implementation class  - [SubmissionPublisher](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/SubmissionPublisher.html) .

Java 11 adds a new reactive [Http Client API](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html) which is based on the Flow APIs. It frees you from using other 3rd party HttpClient libraries, such as Apache HttpClients, OkHttp, etc. when interacts with remote HTTP APIs.

## SmallRye Mutiny 

Since Spring Boot 2.5.5/Spring Framework 5.3.10, [SmallRye Mutiny](https://smallrye.io/smallrye-mutiny) is suppported.

SmallyRye Mutiny is an Reactive Streams implementation from RedHat. 

There are two core types in the SmallRye Mutiny.

* `Multi` - Similar to the Reactor `Flux`, it means there are 0 to many items in the stream. The `Multi` implements `Publisher` interface.
* `Uni` - Similar to the Reactor `Mono`, but Uni accepts `null`. *NOTE: Uni does not implements Publisher.* 

## Spring WebFlux

The Spring embraces [Reactive Streams](http://www.reactive-streams.org/) in the new 5.x era

For Spring developers, it brings a complete new programming model. 

* Spring added a new `spring-webflux` module in it is core framework, and provided built-in reactive programming support via Reactor and RxJava 2/3(RxJava 1 support is removed in the latest Spring 5.3). 
* Spring Security 5 also added reactive feature. 
* Spring supports RSocket which a new bi-direction messaging protocol.
* In Spring Data umbrella projects, a new `ReactiveSortingRepository` interface is added in Spring Data Commons. Redis, Mongo, Cassandra subprojects firstly got reactive supports. For RDBMS, Spring created R2dbc sepc and R2dbc is part of Spring since 5.3. 
* Spring Session also began to add reactive features, an reactive variant for its `SessionRepository` is included since 2.0.
* Spring Integration added flux message channel and reactive programming APIs.
