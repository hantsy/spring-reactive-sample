
# CoWebFilter

`CoWebFilter` is the coroutine variant of the reactive `WebFilter` contract from [Web Programming](../web/web-filter.md). Instead of returning `Mono<Void>`, the `filter` method is a `suspend` function, and the chain exposes a `CoWebFilterChain` whose `filter` call suspends.

## A suspend filter

From the [boot-kotlin-co](https://github.com/hantsy/spring6-sandbox/tree/main/boot-kotlin-co) sample of the spring6-sandbox repository — the same security-check example as the Java [WebFilter](../web/web-filter.md) page, written with coroutines:

```kotlin
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange

@Component
class SecurityWebFilter : CoWebFilter() {
    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        if (!exchange.request.queryParams.containsKey("user")) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        }
        return chain.filter(exchange)
    }
}
```

Declared as a `@Component`, the filter is picked up by component scanning and registered into the `WebHandler` chain — the same behavior as a Java `WebFilter`, with the method body written in sequential coroutine style.

## Testing

The filter is verified with `WebFluxTest` and `runTest` — requests without the `user` parameter get 401, requests with it pass through:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@WebFluxTest(controllers = [HelloController::class])
class WebFilterTests {

    @TestConfiguration
    @Import(value = [SecurityWebFilter::class])
    class TestConfig

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun `authentication failed with no user params`() = runTest {
        client.get().uri("/hello")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `authentication passed with user params`() = runTest {
        client.get().uri("/hello?user=Hantsy")
            .exchange()
            .expectStatus().isOk
    }
}
```

## When to use which

| Contract | Signature | Use when |
| :--- | :--- | :--- |
| `WebFilter` | `Mono<Void> filter(ServerWebExchange, WebFilterChain)` | Java code, or Kotlin code already working with Reactor types |
| `CoWebFilter` | `suspend fun filter(ServerWebExchange, CoWebFilterChain)` | Kotlin coroutines code that prefers sequential, null-safe logic |
