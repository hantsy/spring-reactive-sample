
# An introduction to Reactive Web

Unlike the traditional Servlet stack, the Reactive Web stack introduced in Spring Framework 5 was rewritten from scratch using the Reactive Streams specification and the Reactor API.

There are two important abstractions in the _Reactive Web_ stack that handle a web request.

- [`WebHandler`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/server/WebHandler.html) — the _high-level_ API that assembles the resources such as `WebFilter`, `ExceptionHandler`, and so on to handle web requests from a client.
- [`HttpHandler`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/server/reactive/HttpHandler.html) — the _low-level_ API that adapts web handlers to the underlying runtime environment, such as Netty, as well as Servlet containers which provide async capability.

You can review the `Application` class in the [Getting Started](../start/first.md#bootstrap) chapter to see both abstractions wired together:

```java
HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
HttpServer httpServer = HttpServer.create().host("localhost").port(this.port);
return httpServer.handle(adapter);
```

!!! tip "Base projects for this chapter"

    The examples in this chapter extend the two applications assembled in [Getting Started](../start/intro.md): the [vanilla application](../start/first.md) (manual wiring, no Spring Boot) and the [Spring Boot application](../start/boot-first.md) (`spring-boot-starter-webflux`). Each page names the sample in the repository that holds the complete code.

## Enabling WebFlux

To activate _Reactive Web_, create a `@Configuration` class and add an [`@EnableWebFlux`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/config/EnableWebFlux.html) annotation to enable WebFlux support.

```java
@Configuration
@EnableWebFlux
class WebConfig {}
```

Similar to the existing `WebMvcConfigurer` in the Servlet stack, there is a `WebFluxConfigurer` interface for you to customize the details of web resources, validation, CORS, and so on.

You can create a configuration class to derive this interface and override the methods to customize the built-in configuration.

```java
@Configuration
@EnableWebFlux
class WebConfig implements WebFluxConfigurer {}
```

!!! note

    For Spring Boot applications, no extra steps are required to activate **WebFlux** if _spring-boot-starter-webflux_ is included in the classpath.

## Alternative reactive types

WebFlux is unopinionated about the reactive library: RxJava 3, SmallRye Mutiny, and the JDK `Flow` API can all appear as controller return types, and Spring adapts them automatically. The runnable guides live in the [Reactive libraries](../other/mutiny.md) section of the Reactive Ecosystem chapter:

- [RxJava 3](../other/rxjava.md)
- [SmallRye Mutiny](../other/mutiny.md)
- [The JDK Flow API](../other/java9.md)
