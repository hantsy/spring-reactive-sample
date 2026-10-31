
# Testing

Testing reactive code needs tools that understand asynchrony: assertions on sequences of signals rather than single values, mocks for suspending or publisher-returning methods, and integration tests that never block. The Spring reactive stack ships a layered toolkit, and every sample in this repository exercises at least one layer.

## Chapter roadmap

1. [Testing Web Components](web-components.md) — `WebTestClient` against controllers, exception handling, `RouterFunction`s, and filters, with `@MockitoBean`/`@MockitoSpyBean` for the dependencies.
2. [Data Operations with Testcontainers](testcontainers.md) — real databases in Docker for the data layer: the plain Spring initializer pattern and the Boot `@DynamicPropertySource`/`@ServiceConnection` styles.
3. [Spring Test Context and Slices](spring-test.md) — `SpringJUnitConfig`, `@SpringBootTest`, and the Boot test slices (`@WebFluxTest`, `@JsonTest`, `@DataMongoTest`, and friends).
4. [Testing in Kotlin](kotlin.md) — Kotest, MockK, Spring MockK (`@MockkBean`/`@MockkSpyBean`), and `kotlinx-coroutines-test`.

## StepVerifier: verify a Publisher

`StepVerifier` (from the `reactor-test` module) is the foundation: it subscribes to a `Publisher` and asserts the exact sequence of signals — `onNext` values, then completion or error:

```java
StepVerifier.create(posts.findAll())
    .consumeNextWith(p -> assertEquals("post one", p.getTitle()))
    .consumeNextWith(p -> assertEquals("post two", p.getTitle()))
    .expectComplete()
    .verify();
```

Common patterns used across the samples:

```java
// assert a count of elements
postClient.allPosts()
        .as(StepVerifier::create)
        .expectNextCount(2)
        .verifyComplete();

// assert with custom assertions and cancellation
counter.getInvocationCount()
        .as(StepVerifier::create)
        .consumeNextWith(count -> assertThat(count).isGreaterThan(0))
        .thenCancel()
        .verify();

// build into the call chain with as(StepVerifier::create)
posts.findByTitleLike("one")
    .as(StepVerifier::create)
    .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
    .verifyComplete();
```

The key methods:

- `expectNext(T... values)` / `expectNextCount(long)` — assert the emitted values or their count
- `consumeNextWith(Consumer)` — assert each element with a custom assertion
- `expectError(Class)` / `verifyComplete()` — terminate the verification
- `thenCancel()` — cancel the subscription mid-stream
- `as(StepVerifier::create)` — adapt an existing `Mono`/`Flux` chain into a verifier

`StepVerifier` appears everywhere in this book — from repository tests in the [Data Operations](../data/intro.md) chapter to the `WebClient` tests in [Web Programming](../web/client.md#mocking-a-target-http-api-with-wiremock). The next page moves up the stack to testing the web layer itself.
