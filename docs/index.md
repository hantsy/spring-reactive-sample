# Spring WebFlux Cookbook

A hands-on guide to reactive programming with **Spring Framework 7** and **Spring Boot 4**. Instead of explaining concepts in the abstract, it walks through real examples from the repository and shows how they fit together in a production-style Spring application.

If you want to learn by doing, this is the right place. Each chapter pairs a concise explanation with runnable code from the [repository](https://github.com/hantsy/spring-reactive-sample), so you can read a concept, open the sample, and try it immediately.

## What you will learn

This site covers the reactive stack from the ground up:

- the core ideas behind Reactive Streams, Reactor, and WebFlux
- how to build HTTP APIs with annotated controllers and functional routing
- how to access data reactively with MongoDB, Redis, Cassandra, Couchbase, Neo4j, Elasticsearch, and R2DBC
- how to secure reactive apps and test them with realistic tools
- how Kotlin and the broader reactive ecosystem fit into the same architecture

## Reading path

The chapters build on one another, but each topic is written to stand on its own:

1. **[Preface](preface.md)** — the intent behind the book and how the examples are organized.
2. **[Getting Started](start/intro.md)** — Reactor basics, Reactive Streams, and your first WebFlux app with and without Spring Boot.
3. **[Web Programming](web/intro.md)** — controllers, functional routing, error handling, uploads, SSE, WebSocket, view rendering, `WebClient`, and API versioning.
4. **[Data Operations](data/intro.md)** — reactive data access with MongoDB, Redis, Cassandra, Couchbase, Neo4j, Elasticsearch, and relational databases via R2DBC.
5. **[Security](security/intro.md)** — `ServerHttpSecurity`, method security, custom user services, and form-based authentication.
6. **[Testing](testing/intro.md)** — `StepVerifier`, `WebTestClient`, Testcontainers, and Spring Test integration.
7. **[Kotlin](kotlin/intro.md)** — Kotlin-first WebFlux development with coroutines and DSLs.
8. **[Reactive Ecosystem](other/mutiny.md)** — Mutiny, RxJava 3, JDK Flow, Groovy, caching, events, scheduling, RSocket (TCP, WebSocket, CRUD, Spring Integration), Spring Session, and Spring Integration.

## Start with a sample

Each chapter is backed by a runnable example in the repository. Clone it, start a dependency, and run the module you want to explore.

```bash
git clone https://github.com/hantsy/spring-reactive-sample
cd spring-reactive-sample

docker-compose up mongodb   # start a backing store
cd boot-data-mongo          # choose a sample module
./mvnw spring-boot:run
```

## Repository status

The examples in this repo target **Spring Framework 7 / Spring Boot 4**. If you need older material, the repository still keeps tagged releases for:

- [Spring Boot 3.x](https://github.com/hantsy/spring-reactive-sample/releases/tag/boot-3)
- [Spring Boot 2.x](https://github.com/hantsy/spring-reactive-sample/releases/tag/v1.0)

This keeps the learning path practical: you can move from the modern stack to earlier versions when you need to compare behavior or migrate an older application.
