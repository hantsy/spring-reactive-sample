
# RSocket with Spring Integration

Spring Integration adds RSocket support to its messaging model: an RSocket server can be an Integration flow's inbound gateway, and an Integration flow can drive an outbound RSocket gateway. This page merges the former server and client samples of the [rsocket-sample](https://github.com/hantsy/rsocket-sample/tree/master/integration) repository into a single application — the client flow becomes part of the tests.

The complete example lives in [rsocket-integration](https://github.com/hantsy/spring-reactive-sample/tree/master/rsocket-integration).

## Getting started

Alongside the RSocket starter, bring in Spring Integration and its RSocket module:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-rsocket</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-integration</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-rsocket</artifactId>
</dependency>
```

The server side serves an RSocket server over TCP on port 7000:

```properties
spring.rsocket.server.port=7000
spring.rsocket.server.transport=tcp

# auto configure spring integration rsocket server.
spring.integration.rsocket.server.messageMappingEnabled=true
```

## The server side: an inbound gateway

The application declares a `ServerRSocketConnector` and an `IntegrationFlow` with an RSocket inbound gateway — the flow receives a `Flux<String>` on the `/uppercase` route and transforms every element to uppercase:

```java
@SpringBootApplication
@EnableIntegration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // see PR: https://github.com/spring-projects/spring-boot/pull/18834
    @Bean
    ServerRSocketMessageHandler serverRSocketMessageHandler(RSocketStrategies rSocketStrategies) {
        var handler = new ServerRSocketMessageHandler(true);
        handler.setRSocketStrategies(rSocketStrategies);
        return handler;
    }

    @Bean
    public ServerRSocketConnector serverRSocketConnector(ServerRSocketMessageHandler serverRSocketMessageHandler) {
        return new ServerRSocketConnector(serverRSocketMessageHandler);
    }

    @Bean
    public IntegrationFlow rsocketUpperCaseFlow(ServerRSocketConnector serverRSocketConnector) {
        return IntegrationFlow
                .from(RSockets.inboundGateway("/uppercase")
                        .requestElementType(ResolvableType.forClass(String.class))
                        .interactionModels(RSocketInteractionModel.requestChannel)
                        .rsocketConnector(serverRSocketConnector)
                )
                .<Flux<String>, Flux<String>>transform((flux) -> flux.map(String::toUpperCase))
                .get();
    }
}
```

Reading the wiring:

1. The custom `ServerRSocketMessageHandler` shares the Boot-auto-configured `RSocketStrategies` (codecs) with Integration's message handler.
2. `RSockets.inboundGateway("/uppercase")` maps the `/uppercase` route to the flow; `interactionModels(requestChannel)` restricts it to the channel model — a `Flux` in, a `Flux` out.
3. The `transform` step uppercases every incoming element — any Integration DSL step (`.filter`, `.split`, `.enrichHeaders`, ...) can follow.

!!! note

    Spring Integration 7 removed the `IntegrationFlows` factory class — declare the flows with the static `IntegrationFlow.from(...)` methods instead, as above.

## The client side: an outbound gateway

The former client application declared a `ClientRSocketConnector` pointing at the server and an outbound gateway flow that exposes the remote call as a `Function<Flux<String>, Flux<String>>`:

```java
@Bean
public ClientRSocketConnector clientRSocketConnector() {
    ClientRSocketConnector clientRSocketConnector = new ClientRSocketConnector("localhost", 7000);
    clientRSocketConnector.setAutoStartup(false);
    return clientRSocketConnector;
}

@Bean
public IntegrationFlow rsocketUpperCaseRequestFlow(ClientRSocketConnector clientRSocketConnector) {
    return IntegrationFlow
            .from(Function.class)
            .handle(RSockets.outboundGateway("/uppercase")
                    .interactionModel((message) -> RSocketInteractionModel.requestChannel)
                    .expectedResponseType("T(java.lang.String)")
                    .clientRSocketConnector(clientRSocketConnector))
            .get();
}
```

Merged into one application, the server connector binds port 7000 and the client connector (with `setAutoStartup(false)`) is started on demand — the flow is a bean of type `Function<Flux<String>, Flux<String>>` that any component can inject and apply.

## Testing

The former client's REST/SSE endpoint becomes a test: start the client connector, apply the gateway function, and verify the uppercased channel result:

```java
@SpringBootTest
public class IntegrationTests {

    @Autowired
    @Qualifier("rsocketUpperCaseRequestFlow.gateway")
    Function<Flux<String>, Flux<String>> uppercaseFlow;

    @Autowired
    ClientRSocketConnector clientRSocketConnector;

    @BeforeEach
    public void startClient() {
        if (!clientRSocketConnector.isRunning()) {
            clientRSocketConnector.start();
        }
    }

    @Test
    public void testUppercaseFlow() {
        uppercaseFlow.apply(Flux.just("a", "b", "c", "d"))
                .as(StepVerifier::create)
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
    }
}
```

You can explore the complete codes in [rsocket-integration](https://github.com/hantsy/spring-reactive-sample/tree/master/rsocket-integration).
