
# Getting Started

This chapter walks through the first steps of building Spring WebFlux applications. Start here to get a working app, then deepen your knowledge with the subsequent chapters.

## What this chapter covers

- Core reactive concepts and when to use them
- Building a minimal WebFlux application from scratch (`RouterFunction` + `HandlerFunction`, manual wiring)
- Bootstrapping a WebFlux application with Spring Boot (starters and auto-configuration)
- Running and testing the examples included in this repository

## How to use this chapter

- Follow the tutorials in order if you are new to reactive programming
- Skip the manual wiring if you prefer to use Spring Boot first
- Try the runnable examples in the repository to reinforce each concept

## Chapter roadmap

1. [Reactive Streams and Spring WebFlux](reactive-streams.md) — the specification and the reactive libraries behind WebFlux, with runnable code fragments.
2. [Create a WebFlux application from scratch](first.md) — low-level wiring without Spring Boot auto-configuration, on Tomcat, Jetty, Reactor Netty, or Undertow.
3. [Create a WebFlux application with Spring Boot](boot-first.md) — the fast path using `spring-boot-starter-webflux`.

## Prerequisites

Before you begin, make sure you have:

- JDK 21. Install one from [Adoptium](https://adoptium.net/), Oracle, or your preferred vendor.
- [Apache Maven](https://maven.apache.org) 3.9+ or [Gradle](https://gradle.org)
- A working IDE: [IntelliJ IDEA](https://www.jetbrains.com/idea/), [Eclipse IDE](https://eclipseide.org/) (with [Spring Tools](https://spring.io/tools)), [Apache NetBeans](https://netbeans.apache.org/), or [VS Code](https://code.visualstudio.com/) with the Java and Spring Boot extensions
- Basic familiarity with Spring or Java web development
- Docker, to run the backing stores (MongoDB, Redis, PostgreSQL, and friends) with the `docker-compose.yml` in the repository root

## Running the examples

Every sample in the [repository](https://github.com/hantsy/spring-reactive-sample) is an independent Maven module. To run one:

1. Clone the repository.
2. Change into the sample directory, for example `vanilla`.
3. Run the application with `./mvnw spring-boot:run` for Spring Boot samples, or `./mvnw package exec:java` / the main class for samples wired by hand.

The tutorials are independent and include runnable examples and configuration notes. Refer to the later chapters ([Reactive Web](../web/intro.md), [Data Operations](../data/intro.md), [Security](../security/intro.md)) for integration patterns and production considerations.
