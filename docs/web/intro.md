
# An introduction to Reactive Web

The reactive web stack in Spring is not just a different controller style. It is a different execution model built around non-blocking I/O, backpressure-friendly stream processing, and Reactor primitives such as `Mono` and `Flux`.

At the heart of the stack are two important abstractions:

- [`WebHandler`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/server/WebHandler.html) — the high-level entry point that assembles the pieces of a web request, including filters, exception handlers, and routing logic
- [`HttpHandler`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/server/reactive/HttpHandler.html) — the lower-level bridge between Spring's web layer and the runtime, such as Reactor Netty or a Servlet container with async support

You can see both abstractions working together in the bootstrap code from the [Getting Started](../start/first.md#bootstrap) chapter:

```java
HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
HttpServer httpServer = HttpServer.create().host("localhost").port(this.port);
return httpServer.handle(adapter);
```

!!! tip "Base projects for this chapter"

    The examples in this section extend the two applications introduced earlier in the book: the [vanilla application](../start/first.md) (manual wiring, no Spring Boot) and the [Spring Boot application](../start/boot-first.md) built with `spring-boot-starter-webflux`. Each page points to the exact sample in the repository.

## Enabling WebFlux

To activate the reactive web stack, create a `@Configuration` class and add [`@EnableWebFlux`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/config/EnableWebFlux.html):

```java
@Configuration
@EnableWebFlux
class WebConfig {}
```

If you are familiar with the traditional `WebMvcConfigurer`, you will feel at home with `WebFluxConfigurer`. It gives you a place to customize validation, CORS, resource handling, and other built-in web behaviors:

```java
@Configuration
@EnableWebFlux
class WebConfig implements WebFluxConfigurer {}
```

!!! note

    If you are using Spring Boot, there is usually nothing additional to do: adding `spring-boot-starter-webflux` is enough to activate the stack.

## Alternative reactive types

WebFlux is deliberately library-agnostic. Controller methods can return `Flux`, `Mono`, or types from RxJava 3, SmallRye Mutiny, or the JDK `Flow` API, and Spring adapts them automatically.

The runnable examples for these alternative reactive libraries live in the [Reactive libraries](../other/mutiny.md) section of the ecosystem chapter:

- [RxJava 3](../other/rxjava.md)
- [SmallRye Mutiny](../other/mutiny.md)
- [The JDK Flow API](../other/java9.md)
