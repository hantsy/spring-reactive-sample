
# WebClient and Reactive HTTP Clients

If `RestTemplate` was the usual client in the Servlet era, `WebClient` is the equivalent in the reactive stack. It is non-blocking, composable, and integrates naturally with Reactor streams.

In practice, you use `WebClient` for:

- calling other HTTP services from a reactive app
- streaming request or response bodies
- composing asynchronous flows without blocking a worker thread
- replacing imperative client code with a reactive API that fits the rest of the application

## Getting started

The simplest client is created from a base URL:

```java
WebClient client = WebClient.create("http://localhost:8080");
client
    .get()
    .uri("/posts")
    .retrieve()
    .bodyToFlux(Post.class)
    .log()
    .subscribe(post -> System.out.println("post: " + post));
```

This pattern is enough when you want a quick ad hoc call. For anything larger than a tiny sample, it is worth wrapping the client in a dedicated class.

## Building a client

The builder gives you more control over the underlying connector, codecs, exchange strategies, default headers, and filters.

```java
var reactorHttpClient = HttpClient.create()
        .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG,
                AdvancedByteBufFormat.TEXTUAL)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

WebClient client = WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(reactorHttpClient))
    .codecs(clientCodecConfigurer -> {
        // use defaultCodecs() to apply the default codecs
        // clientCodecConfigurer.defaultCodecs();

        // alter a registered encoder/decoder based on the default config.
        // clientCodecConfigurer.defaultCodecs().jacksonJsonEncoder(...)

        // or use customCodecs to register codecs from scratch.
        clientCodecConfigurer.customCodecs().register(new JacksonJsonDecoder(objectMapper));
        clientCodecConfigurer.customCodecs().register(new JacksonJsonEncoder(objectMapper));
    })
    .exchangeStrategies(ExchangeStrategies.withDefaults())
    .baseUrl("http://localhost:8080")
    .build();
```

The value here is not just configuration. It means the client can match the runtime requirements of the service it talks to while staying reusable in the rest of the application.

## A typed client class

A nicer pattern is to create a dedicated client wrapper instead of calling `WebClient` directly from multiple services. The sample below uses `exchangeToMono` and `exchangeToFlux` to inspect response status and convert error responses into proper reactive results:

```java
@RequiredArgsConstructor
public class PostClient {

    private final WebClient client;

    Flux<Post> allPosts() {
        return client.get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(Post.class));
    }

    Mono<Post> getById(UUID id) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/posts/{id}").build(id))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(Post.class);
                    }
                    return response.createError();
                });
    }

    Mono<ResponseEntity<Void>> save(Post post) {
        return client.post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.CREATED)) {
                        return response.toBodilessEntity();
                    }
                    return response.createError();
                });
    }

    Mono<ResponseEntity<Void>> update(UUID id, Post post) {
        return client.put()
                .uri(uriBuilder -> uriBuilder.path("/posts/{id}").build(id))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        return response.toBodilessEntity();
                    }
                    return response.createError();
                });
    }

    Mono<ResponseEntity<Void>> delete(UUID id) {
        return client.delete()
                .uri(uriBuilder -> uriBuilder.path("/posts/{id}").build(id))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        return response.toBodilessEntity();
                    }
                    return response.createError();
                });
    }
}
```

Then the application can use the client without worrying about low-level transport details:

```java
public static void main(String[] args) {
    var applicationContext = new AnnotationConfigApplicationContext(Application.class);
    var postClient = applicationContext.getBean(PostClient.class);

    postClient.allPosts().subscribe(data -> log.debug("post: {}", data));

    postClient.save(new Post(null, "test", "test content", Status.DRAFT, null))
            .log()
            .flatMap(saved -> {
                var uri = saved.getHeaders().getLocation().toString();
                var strings = uri.split("/");
                return client.getById(UUID.fromString(strings[strings.length - 1]));
            })
            .flatMap(post -> client.update(post.id(),
                    new Post(null, "updated test", "updated content", Status.PENDING_MODERATION, null)))
            .subscribe(responseEntity -> log.debug("updated status: {}", responseEntity));
}
```

You can explore the complete code in [spring-reactive-sample/client](https://github.com/hantsy/spring-reactive-sample/tree/master/client).

## Choosing the HTTP client connector

The `ClientHttpConnector` abstracts the actual HTTP implementation underneath `WebClient`. By default, Spring uses Reactor Netty, but you can switch to other connectors without changing the client code itself.

| Connector | Underlying client | Extra dependency |
| :--- | :--- | :--- |
| `ReactorClientHttpConnector` (default) | Reactor Netty | none (included with WebFlux) |
| `JettyClientHttpConnector` | Jetty Reactive HttpClient | `org.eclipse.jetty:jetty-reactive-httpclient` |
| `JdkClientHttpConnector` | JDK `java.net.http.HttpClient` | none |
| `HttpComponentsClientHttpConnector` | Apache HttpClient 5 | `org.apache.httpcomponents.client5:httpclient5` |

### Reactor Netty (default)

```java
var reactorHttpClient = HttpClient.create()
        .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG,
                AdvancedByteBufFormat.TEXTUAL)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

var connector = new ReactorClientHttpConnector(reactorHttpClient);
```

### Jetty Reactive HttpClient

```java
var jettyHttpClient = new org.eclipse.jetty.client.HttpClient();
jettyHttpClient.setConnectTimeout(5000L);
jettyHttpClient.start();
var connector = new JettyClientHttpConnector(jettyHttpClient);
```

Add the dependency:

```xml
<dependency>
    <groupId>org.eclipse.jetty</groupId>
    <artifactId>jetty-reactive-httpclient</artifactId>
</dependency>
```

### JDK HttpClient

The built-in JDK `java.net.http.HttpClient` needs no extra dependency:

```java
var jvmHttpClient = java.net.http.HttpClient.newBuilder()
        .executor(Executors.newCachedThreadPool())
        .version(java.net.http.HttpClient.Version.HTTP_2)
        .build();
var connector = new JdkClientHttpConnector(jvmHttpClient);
```

### Apache HttpClient 5

```java
var httpAsyncClient = org.apache.hc.client5.http.impl.async.HttpAsyncClients.createHttp2Default();
var connector = new HttpComponentsClientHttpConnector(httpAsyncClient);
```

Add the dependency:

```xml
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
</dependency>
```

Each connector swaps in with one builder call — `.clientConnector(connector)` — while the `PostClient` layer above stays unchanged. You can explore each variant in [client-jetty](https://github.com/hantsy/spring-reactive-sample/tree/master/client-jetty), [client-jdk11-httpclient](https://github.com/hantsy/spring-reactive-sample/tree/master/client-jdk11-httpclient), and [client-apache-httpclient5](https://github.com/hantsy/spring-reactive-sample/tree/master/client-apache-httpclient5).

## Testing the client

The `PostClient` above is tested by stubbing the remote API with [WireMock](https://wiremock.org/) and asserting the reactive sequences — see [Mocking a target HTTP API with WireMock](../testing/web-components.md#mocking-a-target-http-api-with-wiremock) in the Testing chapter. The chapter also covers [`WebTestClient`](../testing/web-components.md) for the server side and [`StepVerifier`](../testing/intro.md#stepverifier-verify-a-publisher) for publisher-level assertions.
