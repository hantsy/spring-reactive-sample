
# Testing

Reactive code is not tested in the same way as blocking code. Instead of asserting one final value, you typically verify a sequence of events: what was emitted, in what order, whether the stream completed, and whether an error appeared at the right time.

This is why the reactive stack comes with a dedicated testing toolkit. The examples in this repository use it throughout the stack, from repository tests to full web integration tests.

## Chapter roadmap

1. [Testing Web Components](web-components.md) — `WebTestClient` against controllers, exception handlers, `RouterFunction`s, and filters, with `@MockitoBean` / `@MockitoSpyBean` for dependencies.
2. [Testing Spring Security](security.md) — mock users, JWT/OIDC-style customization, CSRF handling, and simplified security tests for the web layer.
3. [Data Operations with Testcontainers](testcontainers.md) — real database containers for MongoDB, Redis, PostgreSQL, and friends using the plain Spring initializer and Boot `@ServiceConnection` patterns.
4. [Spring Test Context and Slices](spring-test.md) — `SpringJUnitConfig`, `@SpringBootTest`, and the Boot test slices such as `@WebFluxTest`, `@JsonTest`, and `@DataMongoTest`.
5. [Testing in Kotlin](kotlin.md) — Kotest, MockK, Spring MockK, and coroutine-aware testing helpers.

## StepVerifier: verify a Publisher

`StepVerifier` is the foundation of reactive testing. It subscribes to a `Publisher` and asserts the exact sequence of signals — the values produced, the completion signal, and any error.

```java
StepVerifier.create(posts.findAll())
    .consumeNextWith(p -> assertEquals("post one", p.getTitle()))
    .consumeNextWith(p -> assertEquals("post two", p.getTitle()))
    .expectComplete()
    .verify();
```

This pattern is used all over the repository:

```java
postClient.allPosts()
        .as(StepVerifier::create)
        .expectNextCount(2)
        .verifyComplete();

counter.getInvocationCount()
        .as(StepVerifier::create)
        .consumeNextWith(count -> assertThat(count).isGreaterThan(0))
        .thenCancel()
        .verify();

posts.findByTitleLike("one")
    .as(StepVerifier::create)
    .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
    .verifyComplete();
```

The most useful methods are:

- `expectNext(...)` / `expectNextCount(...)` — assert values or counts
- `consumeNextWith(...)` — run a custom assertion against each emitted element
- `expectError(...)` / `verifyComplete()` — validate completion or failure behavior
- `thenCancel()` — cancel a subscription mid-stream
- `as(StepVerifier::create)` — adapt any existing `Mono` or `Flux` chain for verification

`StepVerifier` is the workhorse for repository tests in the [Data Operations](../data/intro.md) chapter and for HTTP client tests in [Testing Web Components](web-components.md#mocking-a-target-http-api-with-wiremock). The next page moves one level up: testing the web layer itself.
