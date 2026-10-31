
# SmallRye Mutiny

[SmallRye Mutiny](https://smallrye.io/smallrye-mutiny) is a Reactive Streams implementation from Red Hat, supported by Spring WebFlux since Spring Framework 5.3.10. Developers who prefer Mutiny's event-driven API can use its types directly in controllers and repositories.

!!! note "Flow API based"

    Earlier versions of SmallRye Mutiny implemented the Reactive Streams API; current versions use the JDK Flow API instead.

## Add the dependency

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
    <!-- ... -->
</dependencies>
```

## Core types

Mutiny's two core types map directly onto Reactor's:

| Mutiny | Reactor | Emits |
| :--- | :--- | :--- |
| `Uni<T>` | `Mono<T>` | 0..1 item (accepts `null`) |
| `Multi<T>` | `Flux<T>` | 0..N items |

Both implement the Reactive Streams `Publisher` contract, so Spring adapts them automatically wherever a `Publisher` is accepted.

## Use Mutiny in a controller

Return `Uni` and `Multi` from controller methods exactly as you would return `Mono` and `Flux`:

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

Spring handles the adaptation: a `Multi` is streamed like a `Flux`, a `Uni` resolves like a `Mono`, and Mutiny operators (`onItem().transform(...)`, `chain(...)`, and so on) work inside the methods.

For the complete codes, check [spring-reactive-sample/smallrye-mutiny](https://github.com/hantsy/spring-reactive-sample/tree/master/smallrye-mutiny).
