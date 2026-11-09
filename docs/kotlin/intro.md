
# Kotlin

Kotlin is a modern JVM language designed for clarity, safety, and developer productivity. It runs on the Java Virtual Machine, interoperates tightly with Java libraries, and has become a natural fit for Spring applications because it preserves the Java ecosystem while making day-to-day code shorter and easier to read.

At the language level, Kotlin brings several ideas that matter in a Spring application:

- it is built on the JVM, so it can use Spring, Reactor, and the broader Java ecosystem without friction
- it is function-first: higher-order functions, lambdas, default arguments, and concise APIs are first-class features
- it is null-safe: the type system distinguishes nullable and non-nullable values, reducing the classic `NullPointerException` problem in application code
- it has strong support for immutable data: `data class` and `val` make domain models easier to reason about and safer to share across threads
- it is expressive without being noisy: common boilerplate such as getters, setters, and factory methods can be kept out of the codebase

In other words, Kotlin does not replace the JVM or Spring. It gives Spring a more concise and safer language layer on top of the same runtime and ecosystem.

## Kotlin and Spring

Spring has treated Kotlin as a first-class language for a long time, and that support has only strengthened in the Spring 6 era. The framework integrates naturally with Kotlin features such as constructor injection, bean registration, functional routing, nullability-aware APIs, and coroutine support.

This is especially important in reactive applications: Kotlin does not fight the Reactor model; it sits on top of it and makes it easier to express. You can still work with `Mono` and `Flux`, but you can also write code in a more natural Kotlin style with `suspend` functions and `Flow`.

## Kotlin coroutines and Spring WebFlux

Kotlin coroutines are a way to write asynchronous code in a sequential style. Instead of manually nesting callbacks or chaining `Mono`/`Flux`, you can write code that reads like a normal function and let the coroutine runtime handle suspension and resumption.

In Spring WebFlux, this is integrated directly:

- controller methods can be `suspend fun`
- repository methods can return `Flow<T>`
- filters and route handlers can also be coroutine-aware
- Spring internally bridges coroutine-based APIs to Reactor so the non-blocking runtime remains intact

That means a Kotlin controller can look closer to ordinary application logic while still benefiting from the reactive server stack underneath.

The long-term direction is also interesting: Kotlin coroutine support is being treated less as a WebFlux-only feature and more as a general Kotlin capability. In other words, the future direction is to decouple coroutine support from the WebFlux-only layer and make it a broader language/platform feature, while Spring WebFlux remains one of the most natural places to use it.

## Java to Kotlin: a quick comparison

A common Java-style Spring controller looks like this:

```java
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository posts;

    public PostController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable String id) {
        return posts.findById(id);
    }
}
```

The Kotlin version keeps the same Spring behavior but removes much of the ceremony:

```kotlin
@RestController
@RequestMapping("/posts")
class PostController(private val posts: PostRepository) {

    @GetMapping("/{id}")
    suspend fun get(@PathVariable id: String): Post? = posts.findById(id)
}
```

This is the same idea in a more compact form:

- constructor injection becomes a primary constructor parameter
- the class body is shorter
- `suspend` removes the explicit `Mono` wrapper in a controller method
- nullable return types are modeled directly in the type system instead of via extra `switchIfEmpty` logic

A Kotlin data model is also much more natural:

```java
public class Post {
    private String id;
    private String title;
    private String content;

    public Post() {}
    public Post(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
```

```kotlin
data class Post(
    val id: String? = null,
    val title: String,
    val content: String
)
```

The Kotlin version communicates intent immediately: it is an immutable value object, the fields are typed and explicit, and the boilerplate disappears.

## Why this matters in the reactive stack

Reactive programming is not just about `Flux` and `Mono`; it is about modeling asynchronous data flows with clear boundaries and safe composition. Kotlin complements that style because it makes the same ideas feel more direct:

- `Flow<T>` maps naturally to streams of values
- `suspend fun` reads like a regular function while still preserving asynchronous semantics
- nullability helps prevent many runtime surprises at the edge of the application
- domain code stays concise without losing type safety

For example, a coroutine-based repository and controller are often easier to read than a fully explicit Reactor pipeline:

```kotlin
interface PostRepository : CoroutineCrudRepository<Post, Long> {
    fun findByTitleContains(title: String): Flow<Post>
}

@RestController
@RequestMapping("/posts")
class PostController(private val posts: PostRepository) {

    @GetMapping
    fun findAll(): Flow<Post> = posts.findAll()

    @GetMapping("/{id}")
    suspend fun findOne(@PathVariable id: Long): Post? = posts.findById(id)

    @PostMapping
    suspend fun save(@RequestBody post: Post): Post = posts.save(post)
}
```

This keeps the Spring model intact while making the code feel much more natural to Kotlin developers.

## A friendly reading path

If you are new to the Kotlin side of Spring, a good flow is to move from the familiar patterns to the more Kotlin-specific ones:

- start with [Annotation-Driven Controller](annotation-driven-controller.md) to see the Kotlin equivalents of the usual Spring WebFlux code you already know
- then read [Coroutines](coroutines.md) to learn how `suspend` functions, `Flow`, and coroutine-aware Spring APIs make the code feel more natural
- next, explore [Kotlin DSLs](dsl.md) for the more expressive, declarative functional style
- finish with [Events, caching, and scheduling](misc.md) for the practical integration details that show up in real applications

The order is intentional: it starts with the familiar Spring shape, then moves into Kotlin-native idioms, and finishes with the parts that matter most when you build real applications.
