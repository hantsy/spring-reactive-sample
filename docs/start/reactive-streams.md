
# Reactive Streams and Spring WebFlux

Spring WebFlux is the reactive web framework of Spring Framework. It embraces asynchronous, non-blocking I/O and the Reactive Streams specification to build scalable, resource-efficient applications for modern cloud and microservice environments.

This page introduces the Reactive Streams specification and its implementations (Project Reactor, RxJava 3, SmallRye Mutiny, and the Java Flow API), and shows how Spring WebFlux uses them. Everything is demonstrated with short code fragments that you can run with a plain Java `main` method — no server required.

!!! note "Audience and scope"

    This page is for developers new to reactive programming who want to understand the building blocks before writing a WebFlux application. For the application itself, continue to [Create a WebFlux application from scratch](first.md) or [Create a WebFlux application with Spring Boot](boot-first.md).

## Reactive Streams: the specification

[Reactive Streams](https://github.com/reactive-streams/reactive-streams-jvm) — official site [reactive-streams.org](https://www.reactive-streams.org/) — is an initiative to provide a standard for asynchronous stream processing with non-blocking backpressure. It encompasses efforts aimed at runtime environments (JVM and JavaScript) as well as network protocols.

The JVM specification is a small set of four interfaces, defined in the `org.reactivestreams` package:

- [`Publisher<T>`](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html) — produces a potentially unbounded number of sequenced elements, publishing them according to the demand received from its `Subscriber`(s).
- [`Subscriber<T>`](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscriber.html) — receives the signals produced by a `Publisher`: `onSubscribe`, then zero or more `onNext`, and finally `onComplete` or `onError`.
- [`Subscription`](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscription.html) — represents the one-to-one lifecycle between a `Subscriber` and a `Publisher`. It carries the demand signal (`request(long n)`) and `cancel()`.
- [`Processor<T, R>`](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Processor.html) — a processing stage that is both a `Subscriber` and a `Publisher`, obeying the contracts of both.

The interfaces are intentionally tiny:

```java
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> s);
}

public interface Subscriber<T> {
    void onSubscribe(Subscription s);
    void onNext(T t);
    void onError(Throwable t);
    void onComplete();
}

public interface Subscription {
    void request(long n);
    void cancel();
}

public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```

Nothing is delivered until the `Subscriber` signals demand. This is the essence of *backpressure*: the consumer controls the flow of data, so a fast producer cannot overwhelm a slow consumer.

### A minimal Publisher and Subscriber

The following fragment wires a simple `Publisher` to a `Subscriber` by hand. It demonstrates the full signal protocol — no reactive library required, only the `org.reactivestreams` interfaces:

```java
// A Publisher that emits 1, 2, 3 and completes.
Publisher<Integer> publisher = subscriber ->
        subscriber.onSubscribe(new Subscription() {
            private int value = 1;

            @Override
            public void request(long n) {
                for (long i = 0; i < n && value <= 3; i++) {
                    subscriber.onNext(value++);
                }
                if (value > 3) {
                    subscriber.onComplete();
                }
            }

            @Override
            public void cancel() {
            }
        });

publisher.subscribe(new Subscriber<Integer>() {
    @Override
    public void onSubscribe(Subscription s) {
        s.request(3); // signal demand explicitly
    }

    @Override
    public void onNext(Integer item) {
        System.out.println("received: " + item);
    }

    @Override
    public void onError(Throwable t) {
        System.err.println("error: " + t);
    }

    @Override
    public void onComplete() {
        System.out.println("done");
    }
});
```

Running it prints:

```
received: 1
received: 2
received: 3
done
```

In practice you never assemble these interfaces by hand — a reactive library such as Project Reactor does it for you — but the protocol explains what happens behind every `Flux` and `Mono` you will use in WebFlux.

Implementations are validated by a conformance test kit (the [Reactive Streams TCK](https://github.com/reactive-streams/reactive-streams-jvm#build)), which guarantees the interoperability of any compliant library.

## Project Reactor

The [Reactor project](https://projectreactor.io/) works closely with the Spring team, and Reactor is the reactive library that Spring WebFlux uses by default. Useful entry points: the [reference documentation](https://projectreactor.io/docs/core/release/reference/) and the [API Javadoc](https://projectreactor.io/docs/core/release/api/) for [`Flux`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html) and [`Mono`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html).

Reactor provides two types that implement `org.reactivestreams.Publisher`:

- [`Flux<T>`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html) — a `Publisher` that emits 0 to N elements, and then completes either successfully or with an error.
- [`Mono<T>`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html) — a `Publisher` that emits at most one item via `onNext` and then terminates with an `onComplete` signal (a successful `Mono`, with or without a value), or emits only a single `onError` signal (a failed `Mono`).

| Concept | Reactor | Analogy |
| :--- | :--- | :--- |
| 0..1 result, maybe asynchronous | `Mono<T>` | `CompletableFuture<T>` / `Optional<T>` |
| 0..N results, streamed | `Flux<T>` | `Stream<T>` / `Iterable<T>` |

### Creating a Flux and a Mono

Add Reactor to your project (the coordinates are managed by `spring-boot-starter-parent`):

```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>
```

The most common factory methods are static:

```java
Flux<String> flux = Flux.just("a", "b", "c");          // fixed data
Flux<Long> interval = Flux.interval(Duration.ofSeconds(1)); // ticks every second
Flux<Integer> generated = Flux.range(1, 5);            // 1..5

Mono<String> mono = Mono.just("hello");                // single value
Mono<String> empty = Mono.empty();                     // completes without a value
Mono<String> fromCallable = Mono.fromCallable(() -> expensiveBlockingCall()); // lazily wrap blocking code
```

### Subscribing: nothing happens until you subscribe

Reactive types are *lazy* by default. Assembling operators builds a pipeline; subscribing executes it:

```java
Flux.just("a", "b", "c")
    .map(String::toUpperCase)
    .subscribe(
        data -> System.out.println("onNext: " + data),
        err -> System.err.println("onError: " + err),
        () -> System.out.println("onComplete")
    );
```

Output:

```
onNext: A
onNext: B
onNext: C
onComplete
```

!!! warning "Common pitfall"

    A `Flux` or `Mono` that is never subscribed emits nothing. Inside a WebFlux `@RestController` you *return* the reactive type and Spring subscribes for you; in plain Java code you must call `subscribe()` (or a blocking method such as `block()`) yourself.

### Transforming and combining streams

The operator style reads like the Java `Stream` API, but the operations are asynchronous:

```java
Flux.just(1, 2, 3)
    .map(i -> i * 10)                 // transform each element
    .filter(i -> i > 10)              // keep elements matching a predicate
    .flatMap(i -> fetchRelated(i))    // 1-to-N async transformation
    .concatWith(Flux.just(100))       // append another stream
    .take(2)                          // only the first 2 elements
    .subscribe(System.out::println);
```

Combine several streams with `zip` — this pattern appears throughout the sample repository, for example to pace a data stream with a timer:

```java
Flux.zip(Flux.interval(Duration.ofSeconds(1)), Flux.just("a", "b", "c"))
    .map(Tuple2::getT2)
    .subscribe(System.out::println);
// prints "a", then "b", then "c" — one per second
```

### Backpressure: demand in action

Reactor manages demand for you, and exposes it when you want to control it explicitly:

```java
Flux.range(1, 100)
    .subscribe(new BaseSubscriber<Integer>() {
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            request(1); // ask for one item at a time
        }

        @Override
        protected void hookOnNext(Integer value) {
            System.out.println("received: " + value);
            request(1);
        }

        @Override
        protected void hookOnComplete() {
            System.out.println("done");
        }
    });
```

Operators such as `limitRate`, `buffer`, and `onBackpressureDrop` reshape demand when the default behavior does not fit.

### Error handling

Errors are signals, not exceptions thrown at the caller:

```java
Flux.just("a", "b")
    .map(s -> { if (s.equals("b")) throw new IllegalStateException("boom"); return s; })
    .onErrorResume(e -> Flux.just("fallback"))
    .subscribe(System.out::println);
// prints: a, fallback
```

For tests, `reactor-test` provides `StepVerifier`, which verifies the exact sequence of signals:

```java
StepVerifier.create(Flux.just("a", "b"))
    .expectNext("a")
    .expectNext("b")
    .verifyComplete();
```

See [Testing](../testing/intro.md) for a full treatment.

## Other implementations

Spring WebFlux is unopinionated about the reactive library. Any `Publisher` implementation can be a controller return type, and Spring adapts between supported reactive types automatically.

### RxJava 3

[RxJava 3](https://github.com/ReactiveX/RxJava) implements Reactive Streams with `Flowable` (backpressured), `Observable` (unbackpressured), `Single`, `Maybe`, and `Completable`. RxJava 2 is end of life and removed from Spring's supported adapters; prefer RxJava 3 for new work.

```java
// io.reactivex.rxjava3:rxjava
Flowable.just("a", "b", "c")
    .map(String::toUpperCase)
    .subscribe(System.out::println);
```

See [RxJava 3 in a controller](../web/intro.md#rxjava-3) for the WebFlux integration.

### SmallRye Mutiny

[SmallRye Mutiny](https://smallrye.io/smallrye-mutiny/) is a Reactive Streams implementation from Red Hat, supported by Spring since Framework 5.3.10. Its two core types are `Uni<T>` (0..1, similar to `Mono` but accepts `null`) and `Multi<T>` (0..N, similar to `Flux`).

```java
// io.smallrye.reactive:mutiny
Uni.createFrom().item("hello")
    .onItem().transform(String::toUpperCase)
    .subscribe().with(System.out::println);
```

See [Mutiny in a controller](../web/intro.md#smallrye-mutiny) for the WebFlux integration.

### The JDK Flow API

The JDK ships [`java.util.concurrent.Flow`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Flow.html), which packages the Reactive Streams interfaces inside the JDK and adds a ready-made [`SubmissionPublisher`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/SubmissionPublisher.html) implementation. The JDK also includes a reactive [`java.net.http.HttpClient`](https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.html) built on the Flow API.

```java
SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
publisher.subscribe(new Flow.Subscriber<>() {
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(String item) {
        System.out.println("received: " + item);
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
        System.out.println("done");
    }
});

publisher.submit("hello");
publisher.close();
```

Spring adapts between `Flow.Publisher` and Reactive Streams types — see [The JDK Flow API](../web/intro.md#the-jdk-flow-api) and the runnable [java9 sample](https://github.com/hantsy/spring-reactive-sample/tree/master/java9).

### Choosing an implementation

| Library | Core types | Backpressure | Notes |
| :--- | :--- | :--- | :--- |
| Project Reactor | `Mono`, `Flux` | Yes | Default in Spring; richest operator set; `reactor-test` for verification |
| RxJava 3 | `Flowable`, `Observable`, `Single`, `Maybe`, `Completable` | `Flowable` only | Adapted types supported in WebFlux controllers |
| SmallRye Mutiny | `Uni`, `Multi` | Yes | Simple, event-first API; `Uni` accepts `null` |
| JDK Flow | `Flow.Publisher` | Yes | JDK-native; minimal operators; useful for interop |

For new Spring projects, start with Project Reactor: it is what Spring Data, Spring Security, Spring Session, and the WebFlux server internals are built on.

## How Spring WebFlux uses the reactive stack

Spring WebFlux builds on Reactive Streams and Project Reactor by default, but stays unopinionated about the reactive runtime. The key integration points:

- **Reactive return types.** Controller methods and handler functions may return `Mono<T>`, `Flux<T>`, `CompletableFuture<T>`, or any `org.reactivestreams.Publisher<T>`. Spring adapts between supported reactive types automatically.
- **Two programming models.** Annotated controllers (`@RestController` with `@GetMapping`, `@PostMapping`, and friends) or functional handlers (`RouterFunction` + `HandlerFunction`) — see the [Reactive Web](../web/intro.md) chapter.
- **A reactive HTTP client.** `WebClient` performs non-blocking HTTP requests and integrates with Reactor and other reactive types — see [WebClient](../web/client.md).
- **Server runtimes.** Reactor Netty is the default server for Spring Boot's WebFlux starter; Tomcat, Jetty, and Undertow are also supported — see the bootstrapping options in [Create a WebFlux application from scratch](first.md#bootstrap).
- **The wider ecosystem.** Spring Data (R2DBC, reactive repositories), Spring Security (`ServerHttpSecurity`, reactive user details), and Spring Session (reactive `WebSession` storage) all accept and return Reactive Streams types — the following chapters cover each of them.

## References

**Specification**

- [Reactive Streams specification](https://github.com/reactive-streams/reactive-streams-jvm) — the JVM specification, TCK, and examples
- [reactive-streams.org](https://www.reactive-streams.org/) — the official site
- [`org.reactivestreams` Javadoc](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/package-summary.html) — [`Publisher`](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html), [`Subscriber`](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscriber.html), [`Subscription`](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Subscription.html), [`Processor`](http://www.reactivestreams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Processor.html)

**Projects**

- [Project Reactor](https://projectreactor.io/) — [reference docs](https://projectreactor.io/docs/core/release/reference/), [API Javadoc](https://projectreactor.io/docs/core/release/api/), [`Flux`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html), [`Mono`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html)
- [RxJava 3](https://github.com/ReactiveX/RxJava) — [3.x Javadoc](http://reactivex.io/RxJava/3.x/javadoc/)
- [SmallRye Mutiny](https://smallrye.io/smallrye-mutiny/) — [Javadoc](https://javadoc.io/doc/io.smallrye.reactive/mutiny)
- [Reactor Netty](https://projectreactor.io/docs/netty/release/reference/index.html) — the default WebFlux server runtime
- [Spring WebFlux reference](https://docs.spring.io/spring-framework/reference/web-reactive.html) — the official framework documentation

**JDK**

- [`java.util.concurrent.Flow`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Flow.html) and [`SubmissionPublisher`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/SubmissionPublisher.html)
- [`java.net.http.HttpClient`](https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.html) — the JDK's reactive HTTP client

## Where to go next

- [Create a WebFlux application from scratch](first.md) — the low-level building blocks (`HttpHandler`, `WebHttpHandlerBuilder`, Reactor Netty).
- [Create a WebFlux application with Spring Boot](boot-first.md) — the fast path with starters and auto-configuration.
- [An introduction to Reactive Web](../web/intro.md) — the `WebHandler` and `HttpHandler` architecture of WebFlux.
