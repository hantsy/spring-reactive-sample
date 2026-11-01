
# RSocket over WebSocket

WebSocket provides a full-duplex connection over HTTP, making it a useful transport for browser-facing applications. RSocket can run over WebSocket just as it does over TCP.

## Getting started

The dependency is the same RSocket starter as in the TCP example:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-rsocket</artifactId>
</dependency>
```

The transport change is mostly configuration. In `application.properties`, choose WebSocket as the transport and define the path under which the RSocket endpoint is exposed on the web server:

```properties
server.port=8081
# a mapping path is defined
spring.rsocket.server.mapping-path=/rsocket
# websocket is chosen as a transport
spring.rsocket.server.transport=websocket
```

A couple of practical notes:

- With `transport=websocket`, the RSocket endpoint shares the HTTP server (`server.port`) instead of binding its own port.
- `spring.rsocket.server.mapping-path` scopes the endpoint to `/rsocket`, so regular HTTP endpoints — such as a plain `@GetMapping` controller below — can live on the same server.

```java
@RestController
public class HelloController {

    @GetMapping
    public String get() {
        return "server greeting at :" + Instant.now();
    }
}
```

The `@MessageMapping` handlers are the same as in the TCP example. See [RSocket over TCP](tcp.md#serving-routes-with-messagemapping) for the full controller.

## Connecting a client over WebSocket

The client side only changes the transport: `WebsocketClientTransport` points at the mapped path on the web server's port.

```java
@Bean
public RSocketRequester rSocketRequester(RSocketRequester.Builder b) {
    return b.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .connect(WebsocketClientTransport.create(URI.create("ws://localhost:8081/rsocket"))).block();
}
```

After that, the calls are the same as over TCP: `.send()` for fire-and-forget, `.retrieveMono()` and `.retrieveFlux()` for the rest.

## Testing

The test boots the server on a random web port (`RANDOM_PORT`) and connects to the `/rsocket` WebSocket endpoint on that port:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {

    static RSocketRequester requester;

    @LocalServerPort
    int port;

    @BeforeAll
    public static void setup(@Autowired RSocketRequester.Builder builder,
                             @Autowired IntegrationTests tests) {
        requester = builder
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .connect(WebsocketClientTransport.create(
                        URI.create("ws://localhost:" + tests.port + "/rsocket"))).block();
    }

    @Test
    public void testRequestResponseOverWebSocket() {
        requester.route("greet.spring")
                .data(new Greeting("Welcome to RSocket"))
                .retrieveMono(String.class)
                .as(StepVerifier::create)
                .consumeNextWith(msg -> assertTrue(msg.contains("Hello spring")))
                .verifyComplete();
    }
}
```

See the complete example and tests in [rsocket-websocket](https://github.com/hantsy/spring-reactive-sample/tree/master/rsocket-websocket).

## TCP or WebSocket?

| | TCP | WebSocket |
| :--- | :--- | :--- |
| Endpoint | Own port (`spring.rsocket.server.port`) | Shared with the HTTP server |
| Path routing | Not applicable | `spring.rsocket.server.mapping-path` |
| Typical clients | JVM-to-JVM services | Browsers and HTTP-based deployments |
| Configuration | `spring.rsocket.server.transport=tcp` | `spring.rsocket.server.transport=websocket` |

The handler and requester code stays the same either way — only the transport properties and the client transport implementation change.
