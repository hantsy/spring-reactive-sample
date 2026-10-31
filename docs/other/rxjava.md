
# RxJava 3

[RxJava 3](https://github.com/ReactiveX/RxJava) is the modern line of the ReactiveX library for the JVM. Spring WebFlux accepts its types directly in controller methods — Spring adapts them to the Reactive Streams contract internally. RxJava 2 is end of life and removed from Spring's supported adapters; prefer RxJava 3 for new work.

## Add the dependency

```xml
<dependency>
    <groupId>io.reactivex.rxjava3</groupId>
    <artifactId>rxjava</artifactId>
</dependency>
```

## Core types

RxJava's types map onto the Reactive Streams `Publisher` in different ways:

| RxJava 3 | Reactor | Emits | Backpressure |
| :--- | :--- | :--- | :--- |
| `Flowable<T>` | `Flux<T>` | 0..N items, then completes or errors | Yes (`Publisher`) |
| `Observable<T>` | `Flux<T>` | 0..N items | No (unbackpressured) |
| `Single<T>` | `Mono<T>` | Exactly one value or an error | No |
| `Maybe<T>` | `Mono<T>` | 0..1 value or an error | No |
| `Completable` | `Mono<Void>` | Completion only | No |

`Flowable` is the Reactive Streams `Publisher` implementation; the other types bridge through Spring's adapters, which treat them as unbackpressured sources.

## Use RxJava in a controller

The [rxjava3](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava3) sample returns RxJava types from a `@RestController` exactly where Reactor types would sit:

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

The repository behind it mirrors the same types — and shows the full vocabulary:

```java
@Component
class PostRepository {

    Observable<Post> findAll() {
        return Observable.fromIterable(DATA);
    }

    Flowable<Post> flowable() {
        return Flowable.fromIterable(DATA);
    }

    Maybe<Post> findById(UUID id) {
        // ...
                .switchIfEmpty(Maybe.error(new PostNotFoundException(id)));
    }

    Single<Post> save(Post post) {
        return Single.just(saved);
    }
}
```

Spring handles the adaptation on the HTTP boundary — an `Observable`/`Flowable` streams like a `Flux`, a `Single`/`Maybe` resolves like a `Mono`, and empty `Maybe` results map to the empty-response conventions described in [Handling Web Exceptions](../web/exception.md).

## Verify with StepVerifier

Reactor's test tooling works on any Reactive Streams `Publisher`, so a `Flowable` verifies directly; adapter types go through `RxJava3Adapter`:

```java
// Flowable is a Publisher — StepVerifier accepts it as is
StepVerifier.create(posts.findAll())
    .expectNextCount(2)
    .verifyComplete();
```

For the complete codes, check [spring-reactive-sample/rxjava3](https://github.com/hantsy/spring-reactive-sample/tree/master/rxjava3).
