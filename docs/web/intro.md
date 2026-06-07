---
title: An introduction to Reactive Web
parent: Reactive Web
nav_order: 1
toc: true
---

# An introduction to Reactive Web

Unlike the traditional Servlet stack, the new Reactive Web stack that introduced in Spring v5 was rewritten from scratch using ReactiveStreams specification and Reactor API.

There are two important concepts in the _Reactive Web_ stack to handle a web request.

- `WebHandler` - which is the _high-level_ APIs to assemble the resources eg. `WebFilter`, `ExceptionHandler`, etc. to handle web requests from client.
- `HttpHandler` - which is a _low-level_ API to adapt web handlers to the underlying runtime environment, such as Netty, as well as Servlet 3.1+ container which did provide async capability.

You can review the `Application` class in the former [Getting Started](../start) part.

## Enabling WebFlux

To activate _Reactive Web_, create a `@Configuration`class, add an additional `@EnableWebFlux` annotation to enable WebFlux support.

```java
@Configuration
@EnableWebFlux
class WebConfig {}
```

Similar to the existing `WebMvcConfigurer` in the Servlet stack. There is a `WebFluxConfigurer` interface for you to customize the details of web resources, validation, CORS, etc.

You can create a configuration class to derive this interface and override the methods to customize the built-in configuration.

```java
@Configuration
@EnableWebFlux
class WebConfig implements WebFluxConfigurer {}
```

> NOTE: For Spring Boot applications, no extra steps are required to activate **WebFlux** if _spring-boot-starter-webflux_ is included in the classpath.

## RxJava 3

By default, Spring WebFlux heavily depends on Reactor, but it also supports RxJava 3 APIs via adapters.

To use RxJava APIs instead of Reactor's API, firstly add RxJava in project dependencies.

```xml
<dependency>
    <groupId>io.reactivex.rxjava3</groupId>
    <artifactId>rxjava</artifactId>
    <version>3.0.7</version>
</dependency>
```

Then you can use RxJava API freely in the `@RestController` like the APIs in the reactive `Repository` .

```java
@RestController
@RequestMapping(value = "/posts")
class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping(value = "")
    public Observable<Post> all() {
        return this.posts.findAll();
    }

    @GetMapping(value = "/{id}")
    public Maybe<Post> get(@PathVariable(value = "id") UUID id) {
        return this.posts.findById(id);
    }

    @PostMapping(value = "")
    public Single<Post> create(Post post) {
        return this.posts.save(post);
    }
}
```

For the complete codes, check [spring-reactive-sample/rxjava3](https://github.com/hantsy/spring-reactive-sample/blob/master/rxjava3).

## Java 9 Flow API

Java 9 introduced the `java.util.concurrent.Flow` API and tried to standardize it as part of the `java.util.concurrent` package. Spring WebFlux also provide adapter support for it. You can use `Flow.Publisher` as the controller method return type or parameters.

> NOTE: Make sure you are using Java 9 or the later versions.

```java
@RestController
@RequestMapping(value = "/posts")
public class PostController {

    @GetMapping
    public Flow.Publisher<Post> all() {
        // see: https://stackoverflow.com/questions/46597924/spring-5-supports-java-9-flow-apis-in-its-reactive-feature
        return JdkFlowAdapter.publisherToFlowPublisher(
                Flux.just(
                        new Post(1L, "post one", "content of post one"),
                        new Post(2L, "post two", "content of post two")
                )
        );
    }

}
```

For the complete codes, check [spring-reactive-sample/java9](https://github.com/hantsy/spring-reactive-sample/blob/master/java9).

## SmallRye Mutiny

[SmallRye Mutiny](https://smallrye.io/smallrye-mutiny) support is added in Spring Framework 5.3.10. For developers, you can use Mutiny APIs instead of Reactor to build Spring WebFlux applications.

> NOTE: The earlier versions of Smallry Mutiny supported ReactiveStreams APIs, but in the latest version, it swtiched to Java 9 Flow API. So you need to use Java 9 or the later versions to use SmallRye Mutiny.

Add the following project dependencies.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.smallrye.reactive</groupId>
            <artifactId>mutiny-bom</artifactId>
            <version>3.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>

    <dependency>
        <groupId>io.smallrye.reactive</groupId>
        <artifactId>mutiny</artifactId>
    </dependency>
    ...

</dependencies>
```

There is a `Controller` exmaple which is written in SmallRye Mutiny.

```java
@RestController
@RequestMapping(value = "/posts")
class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping(value = "")
    public Multi<Post> all() {
        return this.posts.findAll();
    }

    @GetMapping(value = "/{id}")
    public Uni<Post> get(@PathVariable(value = "id") UUID id) {
        return this.posts.findById(id);
    }

    @PostMapping(value = "")
    public Uni<ResponseEntity<?>> create(@RequestBody Post post) {
        return this.posts.save(post).map(p -> ResponseEntity.created(URI.create("/posts/" + p.getId())).build());
    }

}
```

For the complete codes, check [spring-reactive-sample/smallrye-mutiny](https://github.com/hantsy/spring-reactive-sample/blob/master/smallrye-mutiny).
