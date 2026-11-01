
# Getting Started

This section is the entry point for the book. If you are new to reactive programming, start here. If you already know the basics, use it as a quick check of the foundations before moving into the more specialized chapters.

At a high level, this part is about answering three questions:

- what reactive programming is and why it matters in modern Spring apps
- how `Mono` and `Flux` model asynchronous data streams
- how to build a first WebFlux application, either manually or with Spring Boot

## What you will cover

- the Reactive Streams specification and the role of Project Reactor
- the difference between imperative and reactive code
- minimal WebFlux applications built with `RouterFunction` and `HandlerFunction`
- the same idea expressed with Spring Boot auto-configuration
- the repository examples you can run locally while reading

## Suggested reading path

If you are new to the stack, a sensible order is:

1. [Reactive Streams and Spring WebFlux](reactive-streams.md)
2. [Create a WebFlux application from scratch](first.md)
3. [Create a WebFlux application with Spring Boot](boot-first.md)

This keeps the learning curve gentle: you understand the reactive model first, then move to the Spring Boot convenience layer.

## Prerequisites

Before you start, make sure you have:

- JDK 21 or newer
- [Apache Maven](https://maven.apache.org) 3.9+ or [Gradle](https://gradle.org)
- an IDE such as [IntelliJ IDEA](https://www.jetbrains.com/idea/), [VS Code](https://code.visualstudio.com/), or [Eclipse IDE](https://eclipseide.org/)
- a basic understanding of Spring and Java web development
- Docker for the database-backed examples in the repository

## Running the examples

Every sample in the [repository](https://github.com/hantsy/spring-reactive-sample) is an independent Maven module. You can run them one by one as you work through the book:

1. clone the repository
2. change into the sample directory you want to study, such as `vanilla`
3. run it with `./mvnw spring-boot:run` for Boot-based samples
4. use the module's main class or `./mvnw package exec:java` for the manual wiring samples

The examples are intentionally small and independent, which makes them useful for experimentation. Once the basics are clear, the later chapters on [Reactive Web](../web/intro.md), [Data Operations](../data/intro.md), and [Security](../security/intro.md) show how these ideas work in larger applications.
