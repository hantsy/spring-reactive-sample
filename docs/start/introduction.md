---
title: Introduction to Spring WebFlux
parent: Getting Started
nav_order: 1
toc: true
---

# Introduction to Spring WebFlux

Spring WebFlux is the reactive web framework in Spring Framework. It embraces asynchronous, non-blocking I/O and the Reactive Streams specification to build scalable, resource-efficient applications for modern cloud and microservice environments.

This introduction covers the Reactive Streams specification, common implementations (Project Reactor, RxJava 3, SmallRye Mutiny, and the Java Flow API), and how Spring WebFlux uses these technologies. Many readers find the linked API pages and implementation guides helpful; those links are preserved and expanded below.

## Reactive Streams (the specification)

Reactive Streams is a small set of interfaces that enable interoperable asynchronous stream processing with back pressure. Key interfaces:

- `Publisher<T>` — produces potentially many values asynchronously.
- `Subscriber<T>` — consumes values produced by a `Publisher`.
- `Subscription` — manages demand and cancellation between `Publisher` and `Subscriber`.
- `Processor` — a component that is both a `Subscriber` and a `Publisher`.

Spec and source: https://github.com/reactive-streams/reactive-streams-jvm

Conformance tests (TCK): the Reactive Streams TCK validates implementations. Knowing about the TCK helps when comparing implementations.

## Implementations and libraries

Spring WebFlux interoperates with multiple reactive libraries. The most common are:

- Project Reactor (recommended default)
  - Official site and docs: https://projectreactor.io/
  - API reference: https://projectreactor.io/docs/core/release/api/
  - Types: `Mono<T>` (0..1) and `Flux<T>` (0..N). These implement `org.reactivestreams.Publisher` and offer rich operators for composition and transformation.

- RxJava 3 (adapter-supported)
  - Official site and docs: https://github.com/ReactiveX/RxJava
  - RxJava 3 maps well to Reactive Streams via adapters; **RxJava 2** is deprecated and removed from most modern samples.

- SmallRye Mutiny
  - Project page and docs: https://smallrye.io/smallrye-mutiny/
  - Mutiny offers a simple, fluent API for reactive programming and can be adapted to Reactive Streams types when integrating with WebFlux.

- Java Flow API (`java.util.concurrent.Flow`)
  - JDK (Java 9+) Flow API docs: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/Flow.html
  - `Flow.Publisher`/`Flow.Subscriber` interoperate with other Reactive Streams types via adapters and can be used as controller return types in Spring.

Note: **RxJava 2** is considered legacy; this site removes RxJava 2-focused material and suggests **Project Reactor** or **RxJava 3/Mutiny** as modern alternatives. If retaining historical **RxJava 2** content is desired, those samples can be moved to a clearly marked legacy area.

## How Spring WebFlux uses the Reactive stack

Spring WebFlux builds on Reactive Streams and Project Reactor by default, but remains unopinionated about the underlying reactive runtime. Key points:

- Reactive return types: Controller methods and handler functions may return `Mono<T>`, `Flux<T>`, or any `org.reactivestreams.Publisher<T>`. Spring adapts between supported reactive types automatically.

- Programming models:
  - Annotated controllers (`@Controller` / `@RestController`) using `@GetMapping`, `@PostMapping`, etc.
  - Functional handlers (`RouterFunction` + `HandlerFunction`) for a lightweight, functional style.

- Web clients and servers:
  - `WebClient` — a non-blocking, reactive HTTP client that integrates with Reactor and other reactive types. Docs: https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client
  - Server runtimes — **Reactor Netty** is the default server for Spring Boot's WebFlux starter. **Eclipse Jetty** is supported as an alternative. **Undertow** is retained only in historical samples and is marked *deprecated)* here. Prefer **Reactor Netty** for new projects.

- Integration with other Spring projects:
  - Spring Data (`R2DBC`, reactive repositories) for non-blocking database access.
  - Spring Security reactive support for securing applications with `ServerHttpSecurity` and reactive user services.
  - Spring Session and other modules provide reactive-friendly APIs when available.

## Useful links and references

- Reactive Streams spec: https://github.com/reactive-streams/reactive-streams-jvm
- Project Reactor: https://projectreactor.io/ (Core API: `Mono`, `Flux`)
- RxJava (ReactiveX): https://github.com/ReactiveX/RxJava
- SmallRye Mutiny: https://smallrye.io/smallrye-mutiny/
- Java Flow API (JDK): https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/Flow.html
- Spring WebFlux reference (Spring Framework): https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html
- Spring Boot WebFlux starter: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#web.reactive

## Where to start in this guide

- [Create a WebFlux application with Spring Boot][./boot-first.md] — fastest path using Spring Boot 4 starters and auto-configuration.
- [Create a WebFlux application from Scratch](./first.md) — learn the low-level building blocks (`RouterFunctions`, `HandlerFunctions`, and Reactor primitives).

Both tutorials in this book assume **Project Reactor** as the default reactive library but show how to adapt other libraries when needed. For historical or migration context, legacy samples are available but clearly marked as deprecated.

If any of the original API links or examples you recall are missing, say which ones and they will be re-added or expanded with direct anchors to the API docs.