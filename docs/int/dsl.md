
# Spring Integration Java DSL

The [IntegrationFlow](https://docs.spring.io/spring-integration/reference/dsl.html) DSL replaces endpoint and channel beans with a fluent declaration. This page rebuilds the flow from the [previous page](webflux.md) — `GET /all` inbound gateway → reactive channel → `WebFlux` outbound gateway → `GET /posts` — with the DSL.

## The flow definition

```java
@Configuration
@EnableIntegration
@IntegrationComponentScan
public class IntegrationDslConfig {

    @Bean
    public IntegrationFlow inboundChannelAdapterFlow() {
        return IntegrationFlow
                .from(
                        WebFlux
                                .inboundGateway("/all")
                                .requestMapping(m -> m.methods(HttpMethod.GET))
                )
                .channel(c -> c.flux("outboundReactive.input"))
                .get();
    }

    @Bean
    public IntegrationFlow outboundReactive() {
        return f -> f
                .handle(
                        WebFlux
                                .<MultiValueMap<String, String>>outboundGateway(
                                        m -> UriComponentsBuilder
                                                .fromUriString("http://localhost:8080/posts")
                                                .build()
                                                .toUri()
                                )
                                .httpMethod(HttpMethod.GET)
                                .expectedResponseType(String.class)
                );
    }
}
```

Reading the DSL:

1. `WebFlux.inboundGateway("/all")` declares the HTTP entry point — equivalent to the `WebFluxInboundEndpoint` bean from the annotation-based version, including its `RequestMapping`.
2. `.channel(c -> c.flux("outboundReactive.input"))` bridges the gateway to the second flow through a `FluxMessageChannel` created on the fly and named after the target flow's input channel. With the DSL there is no need to declare the `FluxMessageChannel` bean by hand.
3. `WebFlux.outboundGateway(...)` is the DSL counterpart of `WebFluxRequestExecutingMessageHandler`: it executes a `GET http://localhost:8080/posts` through a `WebClient` and delivers the response payload, typed by `expectedResponseType`, to the flow's output.

The dependency list and the application skeleton are identical to the [annotation-based sample](webflux.md); only the configuration class changes.

## Verify

The test is the same as the annotation-based sample — both routes return 200:

```java
@Test
public void getAllPostsWillBeOk_viaIntegration() {
    this.rest
            .get()
            .uri("/all")
            .exchange()
            .expectStatus().isOk();
}
```


## What's next

The DSL scales to real pipelines: add `.filter(...)`, `.transform(...)`, `.split(...)` between the gateway and the outbound gateway, and swap the outbound target for any messaging middleware. For channel patterns that connect Integration with Redis pub/sub and Reactor `Sinks`, see [Reactive Message Channels](message-channel.md).

## Sample

For the complete codes, check [spring-reactive-sample/integration-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/integration-dsl).
