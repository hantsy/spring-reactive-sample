
# RSocket with Spring Integration

Spring Integration adds first-class RSocket support to its messaging model: an RSocket server can act as an inbound gateway in an Integration flow, and an Integration flow can drive an outbound RSocket gateway. This tutorial combines the former server and client examples from the [rsocket-sample](https://github.com/hantsy/rsocket-sample/tree/master/integration) repository into one application, with the client flow covered by tests.

The complete example lives in [integration-rsocket](https://github.com/hantsy/spring-reactive-sample/tree/master/integration-rsocket).

## Getting started

Alongside the RSocket starter, add Spring Integration and its RSocket support:

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

The server exposes an RSocket endpoint over TCP on port 7000:

```properties
spring.rsocket.server.port=7000
spring.rsocket.server.transport=tcp

# auto configure spring integration rsocket server.
spring.integration.rsocket.server.messageMappingEnabled=true
```

## The server side: an inbound gateway

The application declares a `ServerRSocketConnector` and an `IntegrationFlow` with an RSocket inbound gateway. The flow receives a `Flux<String>` on the `/uppercase` route and transforms every element to uppercase:

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

1. The custom `ServerRSocketMessageHandler` shares the Boot auto-configured `RSocketStrategies` (including codecs) with Integration's message handler.
2. `RSockets.inboundGateway("/uppercase")` maps the `/uppercase` route to the flow; `interactionModels(requestChannel)` narrows it to the channel model, which means a `Flux` in and a `Flux` out.
3. The `transform` step uppercases every incoming element. After that, any Integration DSL step such as `.filter`, `.split`, or `.enrichHeaders` can follow.

!!! note

    Spring Integration 7 removed the `IntegrationFlows` factory class. Declare the flows with the static `IntegrationFlow.from(...)` methods instead, as shown above.

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

In a single Spring Boot application, the server connector binds port 7000 and the client connector (with `setAutoStartup(false)`) starts on demand. The result is a bean of type `Function<Flux<String>, Flux<String>>` that other components can inject and call directly.

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

You can explore the complete code in [integration-rsocket](https://github.com/hantsy/spring-reactive-sample/tree/master/integration-rsocket).
