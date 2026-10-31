
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

## RxJava 3

By default, Spring WebFlux heavily depends on Reactor, but it also supports [RxJava 3](https://github.com/ReactiveX/RxJava) APIs via adapters.

To use RxJava APIs instead of Reactor's API, firstly add RxJava in project dependencies.

```xml
<dependency>
    <groupId>io.reactivex.rxjava3</groupId>
    <artifactId>rxjava</artifactId>
</dependency>
```

Then you can use RxJava API freely in the `@RestController` like the APIs in the reactive `Repository`.

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

For the complete codes, check [spring-reactive-sample/rxjava3](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava3).

## The JDK Flow API

The [`java.util.concurrent.Flow`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Flow.html) API packages the Reactive Streams interfaces inside the JDK. Spring WebFlux provides adapter support for it, so you can use `Flow.Publisher` as a controller method return type or parameter.

```java
@RestController
@RequestMapping(value = "/posts")
public class PostController {

    @GetMapping
    public Flow.Publisher<Post> all() {
        return JdkFlowAdapter.publisherToFlowPublisher(
                Flux.just(
                        new Post(1L, "post one", "content of post one"),
                        new Post(2L, "post two", "content of post two")
                )
        );
    }

}
```

For the complete codes, check [spring-reactive-sample/java9](https://github.com/hantsy/spring-reactive-sample/tree/master/java9).

## SmallRye Mutiny

[SmallRye Mutiny](https://smallrye.io/smallrye-mutiny/) support is added in Spring Framework 5.3.10. For developers, you can use Mutiny APIs instead of Reactor to build Spring WebFlux applications.

!!! note

    Earlier versions of SmallRye Mutiny implemented the Reactive Streams API; current versions use the JDK Flow API instead.

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

There is a `Controller` example which is written in SmallRye Mutiny.

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

For the complete codes, check [spring-reactive-sample/smallrye-mutiny](https://github.com/hantsy/spring-reactive-sample/tree/master/smallrye-mutiny).
