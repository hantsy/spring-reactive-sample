# Reactive Programming with Spring WebFlux

This book and sample repository introduce reactive programming with Spring Framework 7 and Spring Boot 4. It is a practical guide for developers who want to learn Spring WebFlux, Reactor, and the wider reactive ecosystem through runnable examples.

Every concept is demonstrated with a small, self-contained sample from the [repository](https://github.com/hantsy/spring-reactive-sample), and every code fragment in these pages is taken from the actual sample code — clone the repository and run each sample as you read.

## Reading order

The chapters build on each other, but each page stands on its own:

1. **[Preface](preface.md)** — who this book is for and how the examples are organized.
2. **[Getting Started](start/intro.md)** — the Reactive Streams specification, Project Reactor's `Mono`/`Flux`, and your first WebFlux application — with and without Spring Boot.
3. **[Web Programming](web/intro.md)** — annotated controllers and functional routing, exception handling, multipart uploads, Server-Sent Events, WebSocket, view rendering, and the `WebClient` HTTP client.
4. **[Data Operations](data/intro.md)** — reactive repositories and templates for MongoDB, Redis, Cassandra, Couchbase, Elasticsearch, Neo4j, and R2DBC for relational databases, plus the native reactive drivers.
5. **[Security](security/intro.md)** — `ServerHttpSecurity`, method-level constraints, custom user details services, and form login.
6. **[Session](session/intro.md)** — reactive `WebSession` with in-memory, Redis, and MongoDB stores, plus header-based session ids.
7. **[Integration](int/intro.md)** — Spring Integration with reactive message channels, the `IntegrationFlow` DSL, and channel patterns across the ecosystem.
8. **[Kotlin](kotlin/intro.md)** — annotated controllers and `RouterFunctions` in Kotlin, coroutines, the beans/`coRouter` DSLs, `CoWebFilter`, and coroutine-friendly events, caching, and scheduling.
9. **[Other Topics](other/groovy.md)** — Groovy, caching, events, scheduling, RestDocs, testing, Mutiny, and the Flow API.

## Running the samples

Clone the repository and start any sample as described in [Getting Started — Running the examples](start/intro.md#running-the-examples). Backing stores (MongoDB, Redis, PostgreSQL, and friends) are provided by the `docker-compose.yml` in the repository root.

```bash
git clone https://github.com/hantsy/spring-reactive-sample
cd spring-reactive-sample
docker-compose up mongodb   # start a backing store
cd boot-data-mongo          # pick a sample
./mvnw spring-boot:run
```

The current source codes target **Spring Framework 7 / Spring Boot 4**. Older releases are tagged in the repository: [boot-3](https://github.com/hantsy/spring-reactive-sample/releases/tag/boot-3) for Spring Boot 3.x and [v1.0](https://github.com/hantsy/spring-reactive-sample/releases/tag/v1.0) for Spring Boot 2.x.
