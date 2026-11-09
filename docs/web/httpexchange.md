# HTTP Interfaces with @HttpExchange

A typed client wrapper, as shown in the [WebClient chapter](client.md), keeps HTTP details in one class — but you still write the request-building code yourself. Spring's HTTP interface approach removes that last layer: you declare a Java interface annotated with `@HttpExchange`, and Spring creates a proxy that performs the requests through `WebClient`.

The interface becomes the contract with the remote API. Method annotations describe the HTTP method, URL, and media types; return types stay reactive (`Mono`, `Flux`, or `ResponseEntity` wrappers), so the client composes with the rest of a WebFlux application exactly like hand-written `WebClient` code.

Spring Boot 4 adds auto-configuration for these HTTP service clients: `@ImportHttpServices` registers the interfaces, and `spring.http.serviceclient.*` properties configure each group.

The examples referenced here live in this repository: [httpexchange-webclient](https://github.com/hantsy/spring-reactive-sample/tree/master/httpexchange-webclient) (manual configuration) and [boot-httpexchange-webclient](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-httpexchange-webclient) (Boot starter and properties).

## Declare an HTTP interface

The domain model is a simple record:

```java
public record Post(UUID id,
                   String title,
                   String content,
                   Status status,
                   LocalDateTime createdAt
) {
}
```

The interface maps each operation to an HTTP request. `@HttpExchange` on the type sets the common URL prefix, and the method-level annotations (`@GetExchange`, `@PostExchange`, `@PutExchange`, `@DeleteExchange`) describe individual calls:

```java
@HttpExchange(url = "/posts")
public interface PostHttpService {
    @GetExchange(accept = MediaType.APPLICATION_JSON_VALUE)
    Flux<Post> allPosts();

    @GetExchange(value = "/{id}", accept = MediaType.APPLICATION_JSON_VALUE)
    Mono<Post> getById(@PathVariable("id") UUID id);

    @PostExchange(contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> save(@RequestBody Post post);

    @PutExchange(value = "/{id}", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> update(@PathVariable UUID id, @RequestBody Post post);

    @DeleteExchange(value = "/{id}")
    Mono<ResponseEntity<Void>> delete(@PathVariable UUID id);
}
```

The familiar WebMVC annotations `@PathVariable` and `@RequestBody` bind method arguments to URI templates and request bodies. Returning `Mono<ResponseEntity<Void>>` keeps access to status codes and headers — useful for reading the `Location` header of a `201 Created` response.

## Manual configuration

Without Spring Boot, create the proxy yourself. Build a `WebClient`, wrap it in a `WebClientAdapter`, and let `HttpServiceProxyFactory` create the interface implementation:

```java
@Configuration
public class ClientConfig {

    @Bean
    WebClient webClient(JsonMapper mapper) {
        var reactorHttpClient = HttpClient.create()
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(reactorHttpClient))
                .codecs(clientCodecConfigurer -> {
                    clientCodecConfigurer.customCodecs()
                            .register(new JacksonJsonDecoder(mapper));
                    clientCodecConfigurer.customCodecs()
                            .register(new JacksonJsonEncoder(mapper));
                })
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("http://localhost:9090")
                .build();
    }

    @Bean
    PostHttpService postHttpService(WebClient webClient) {
        var proxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
        return proxyFactory.createClient(PostHttpService.class);
    }
}
```

The `JsonMapper` bean configures Jackson 3 (`tools.jackson`) — the codecs `JacksonJsonEncoder` and `JacksonJsonDecoder` use it for request and response bodies. From this point on, inject `PostHttpService` like any other bean; no `WebClient` call sites remain in the application code:

```java
var postHttpService = applicationContext.getBean(PostHttpService.class);

postHttpService.allPosts()
        .subscribe(post -> log.debug("post: {}", post));

postHttpService.save(new Post(null, "test", "test content", Status.DRAFT, null))
        .flatMap(saved -> {
            var uri = saved.getHeaders().getLocation().toString();
            var strings = uri.split("/");
            return postHttpService.getById(UUID.fromString(strings[strings.length - 1]));
        })
        .subscribe(post -> log.debug("saved post: {}", post));
```

You can explore the complete code in [httpexchange-webclient](https://github.com/hantsy/spring-reactive-sample/tree/master/httpexchange-webclient).

## Spring Boot auto-configuration

In a Boot application, the starter and a registration annotation replace the manual proxy setup. Add the dependencies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webclient</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jackson</artifactId>
</dependency>
```

Register the interface with `@ImportHttpServices`, assigning it to a named group and selecting the reactive client type:

```java
@SpringBootApplication
@ImportHttpServices(
        value = PostHttpService.class,
        group = "post",
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

Boot creates the proxy bean and applies the group's settings from `application.properties`:

```properties
spring.http.serviceclient[post].base-url=http://localhost:9090

# shared settings for the reactive HTTP client
#spring.http.reactiveclient.connect-timeout=3s
#spring.http.reactiveclient.read-timeout=1s
#spring.http.reactiveclient.redirects=dont_follow

# requires the Jetty reactive HTTP client on the classpath
#spring.http.reactiveclient.connector=jetty
```

The group name in `@ImportHttpServices(group = "post")` is the key in `spring.http.serviceclient[post].*`, so each group can point at a different service.

To tune the underlying `WebClient` — codecs, default headers, filters — contribute a `WebClientCustomizer` bean instead of building the client by hand:

```java
@Configuration(proxyBeanMethods = false)
public class WebClientConfig {

    @Bean
    WebClientCustomizer webClientCustomizer(JsonMapper mapper) {
        return builder -> builder
                .codecs(c -> c.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(mapper)))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }

    @Bean
    ClientHttpConnectorBuilder<?> clientHttpConnectorBuilder() {
        return ClientHttpConnectorBuilder.reactor();
    }
}
```

You can explore the complete code in [boot-httpexchange-webclient](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-httpexchange-webclient).

## Testing with WireMock

The proxy is a normal bean, so tests stub the remote API with [WireMock](https://wiremock.org/) and assert the reactive sequences with `StepVerifier` — the same approach as the typed client in the [WebClient chapter](client.md#testing-the-client). In the Boot example, `@SpringBootTest` loads the auto-configured proxy; in the manual example, `@SpringJUnitConfig(ClientConfig.class)` loads the hand-built beans:

```java
@SpringBootTest
@WireMockTest(httpPort = 9090)
public class PostHttpServiceTest {

    @Autowired
    PostHttpService client;

    @Test
    public void testGetAllPosts() {
        var data = List.of(
                new Post(UUID.randomUUID(), "title1", "content1", Status.DRAFT, LocalDateTime.now()),
                new Post(UUID.randomUUID(), "title2", "content2", Status.PUBLISHED, LocalDateTime.now())
        );
        stubFor(get("/posts")
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withResponseBody(Body.fromJsonBytes(Json.toByteArray(data)))
                )
        );

        client.allPosts()
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();

        verify(getRequestedFor(urlEqualTo("/posts"))
                .withHeader("Accept", equalTo("application/json")));
    }
}
```

The WireMock port matches the configured `base-url`, so the proxy calls the stub server without any test-specific client configuration. See [Mocking a target HTTP API with WireMock](../testing/web-components.md#mocking-a-target-http-api-with-wiremock) in the Testing chapter for the general pattern, and [StepVerifier](../testing/intro.md#stepverifier-verify-a-publisher) for publisher-level assertions.