
# WebFlux endpoints with Spring Integration

This page assembles a reactive Integration flow with annotations: a `WebFluxInboundEndpoint` serves `GET /all`, forwards each request through a `FluxMessageChannel`, and a `WebFluxRequestExecutingMessageHandler` calls the app's own `/posts` endpoint through a `WebClient` and streams the reply back.

## Step 1: enable Integration

Add the dependency (see the [chapter intro](intro.md)) and annotate a configuration class:

```java
@Configuration
@EnableIntegration
@IntegrationComponentScan
public class IntegrationConfig {

    // endpoint beans below
}
```

## Step 2: declare the inbound endpoint

```java
@Bean
public WebFluxInboundEndpoint jsonInboundEndpoint() {
    WebFluxInboundEndpoint endpoint = new WebFluxInboundEndpoint();
    RequestMapping requestMapping = new RequestMapping();
    requestMapping.setPathPatterns("/all");
    endpoint.setRequestMapping(requestMapping);
    endpoint.setRequestChannel(fluxResultChannel());
    return endpoint;
}

@Bean
public MessageChannel fluxResultChannel() {
    return new FluxMessageChannel();
}
```

The endpoint maps `GET /all` to Integration messages and writes the replies of `fluxResultChannel` to the HTTP response. Because the channel is a `FluxMessageChannel`, the reply can be a streamed `Flux` with backpressure honored end to end.

## Step 3: declare the reactive outbound handler

```java
@Bean
public WebClient webClient() {
    return WebClient.builder().baseUrl("http://localhost:8080").build();
}

@ServiceActivator(inputChannel = "fluxResultChannel")
@Bean
public WebFluxRequestExecutingMessageHandler reactiveOutbound(WebClient client) {
    WebFluxRequestExecutingMessageHandler handler
            = new WebFluxRequestExecutingMessageHandler("/posts", client);
    handler.setHttpMethod(HttpMethod.GET);
    handler.setExpectedResponseType(String.class);
    return handler;
}
```

The `@ServiceActivator` subscribes the handler to the `fluxResultChannel`: every message that arrives on the channel triggers an HTTP `GET /posts` through the `WebClient`, and the reactive reply flows back through the inbound endpoint to the original caller.

## Step 4: verify

The sample's repository serves two posts from memory, and the test asserts both the plain WebFlux route and the Integration route:

```java
@SpringJUnitConfig(classes = Application.class)
public class IntegrationTests {

    @Autowired
    HttpServer httpServer;

    @BeforeEach
    public void setup() {
        this.disposableServer = this.httpServer.bindNow();
        this.client = WebTestClient
                .bindToServer()
                .responseTimeout(Duration.ofMillis(5000))
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    public void getAllPostsWillBeOk() {
        this.client
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getAllPostsWillBeOk_viaIntegration() {
        this.client
                .get()
                .uri("/all")
                .exchange()
                .expectStatus().isOk();
    }
}
```

For the complete codes, check [spring-reactive-sample/integration](https://github.com/hantsy/spring-reactive-sample/tree/master/integration).

## What's next

The annotation style works, but the wiring is verbose. The [Spring Integration Java DSL](dsl.md) expresses the same flow in a few fluent lines.
