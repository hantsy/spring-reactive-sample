
# RSocket over TCP

The TCP examples from [hantsy/rsocket-sample](https://github.com/hantsy/rsocket-sample/tree/master/tcp) have been ported to the [rsocket-tcp](https://github.com/hantsy/spring-reactive-sample/tree/master/rsocket-tcp) module in this repository. They demonstrate all four interaction models, with tests exercising the client-side calls.

## Getting started

The fastest way to begin is to generate a project from [Spring Initializr](https://start.spring.io), as described in the [Getting Started](../../start/intro.md) chapter. Select **Spring WebFlux**, **RSocket**, and **Lombok** — the resulting dependency is the RSocket starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-rsocket</artifactId>
</dependency>
```

Configure the server to accept RSocket connections over TCP on port 7000 in `application.properties`:

```properties
spring.rsocket.server.port=7000
spring.rsocket.server.transport=tcp
```

## Defining the message payload

The client and server exchange a very small payload object:

```java
@Data
class Greeting {
    String message;
}
```

## Serving routes with @MessageMapping

`@MessageMapping` handles incoming RSocket routes the same way `@GetMapping` handles HTTP paths. The return type tells Spring which interaction model to use:

```java
@Controller
@Slf4j
class GreetingServerController {

    @MessageMapping("hello")
    public Mono<Void> hello(Greeting p) {
        log.info("received: {} at {}", p, Instant.now());
        return Mono.empty();
    }

    @MessageMapping("greet.{name}")
    public Mono<String> greet(@DestinationVariable String name, @Payload Greeting p) {
        log.info("received: {}, {} at {}", name, p, Instant.now());
        return Mono.just("Hello " + name + ", " + p.getMessage() + " at " + Instant.now());
    }

    @MessageMapping("greet-stream")
    public Flux<String> greetStream(@Payload Greeting p) {
        log.info("received:  {} at {}", p, Instant.now());
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "greet-stream#(Hello #" + i + "," + p.getMessage() + ") at " + Instant.now());
    }

    @MessageMapping("greet-channel")
    public Flux<String> greetChannel(@Payload Flux<Greeting> p) {
        log.info("received:  {} at {}", p, Instant.now());
        return p.delayElements(Duration.ofSeconds(1))
                .map(m -> "greet-channel#(" + m + ") at " + Instant.now());
    }
}
```

Reading the handlers:

1. `hello` — a **fire-and-forget** route: the `Mono<Void>` completes when the payload is consumed, and nothing is sent back.
2. `greet.{name}` — a **request/response** route: `@DestinationVariable` captures the `.{name}` segment, and the `Mono<String>` reply is sent back to the caller.
3. `greet-stream` — a **request/stream** route: one incoming `Greeting` yields an endless `Flux<String>` of periodic greetings.
4. `greet-channel` — a **channel** route: the handler receives a `Flux<Greeting>` from the client and returns a transformed `Flux<String>`, creating a bidirectional exchange.

Start the server:

```bash
mvn spring-boot:run
```

## Calling the routes with RSocketRequester

The client side uses an `RSocketRequester`, built from the auto-configured `Builder` and a Netty TCP transport:

```java
@Bean
public RSocketRequester rSocketRequester(RSocketRequester.Builder b) {
    return b.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .connect(TcpClientTransport.create("localhost", 7000)).block();
}
```

Then call the routes from a REST controller — each interaction model exposed behind a tiny HTTP endpoint:

```java
@Slf4j
@RestController()
@RequiredArgsConstructor
class GreetingController {

    private final RSocketRequester requester;

    @GetMapping("hello")
    Mono<Void> hello() {
        return this.requester.route("hello").data(new Greeting("Welcome to RSocket")).send();
    }

    @GetMapping("name/{name}")
    Mono<String> greet(@PathVariable String name) {
        return this.requester.route("greet." + name)
                .data(new Greeting("Welcome to RSocket"))
                .retrieveMono(String.class)
                .doOnNext(msg -> log.info("received message::" + msg));
    }

    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> greetStream() {
        return this.requester.route("greet-stream").data(new Greeting("Welcome to RSocket"))
                .retrieveFlux(String.class)
                .doOnNext(msg -> log.info("received messages::" + msg));
    }

    @GetMapping(value = "channel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> greetChannel() {
        return this.requester.route("greet-channel")
                .data(Flux.range(0, 10).map(i -> new Greeting("Welcome to RSocket #" + i)))
                .retrieveFlux(String.class)
                .doOnNext(msg -> log.info("received messages::" + msg));
    }
}
```

The client-side verbs are straightforward: `.send()` for fire-and-forget calls, `.retrieveMono()` for a single reply, `.retrieveFlux()` for streams and channels, and `.data(Flux...)` when the upstream is a channel.

## Testing

In the ported module, the client code becomes tests. We connect with the builder and verify each interaction model with `StepVerifier`:

```java
@SpringBootTest
public class IntegrationTests {

    static RSocketRequester requester;

    @BeforeAll
    public static void setup(@Autowired RSocketRequester.Builder builder) {
        requester = builder
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .connect(TcpClientTransport.create("localhost", 7000)).block();
    }

    @Test
    public void testRequestResponse() {
        requester.route("greet.spring")
                .data(new Greeting("Welcome to RSocket"))
                .retrieveMono(String.class)
                .as(StepVerifier::create)
                .consumeNextWith(msg -> assertTrue(msg.contains("Hello spring")))
                .verifyComplete();
    }

    @Test
    public void testRequestChannel() {
        Flux<Greeting> payloads = Flux.range(0, 10)
                .map(i -> new Greeting("Welcome to RSocket #" + i));

        requester.route("greet-channel")
                .data(payloads)
                .retrieveFlux(String.class)
                .take(10)
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();
    }
}
```

The `@LocalRSocketServerPort` annotation from earlier Boot versions no longer exists in Boot 4, so the tests connect to the fixed port configured in `application.properties`.

See the complete example and tests in [rsocket-tcp](https://github.com/hantsy/spring-reactive-sample/tree/master/rsocket-tcp).
