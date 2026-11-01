
# WebClient and Reactive HTTP Clients

In the Servlet stack, `RestTemplate` and `AsyncRestTemplate` perform HTTP requests. In the WebFlux stack, Spring adds `WebClient` — a non-blocking, reactive HTTP client that integrates with Reactor and other reactive types.

`WebClient` supports streaming request and response bodies, runs on several pluggable HTTP client libraries, and is the foundation for consuming other reactive services.

## Getting started

The simplest way to create a client is a static factory. The following example sends a `GET` request to the `/posts` URI and retrieves posts:

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

## Building a client

For more control, use the builder. It lets you configure the `ClientHttpConnector` (the underlying HTTP client), the codecs, `ExchangeStrategies`, default headers, and filters:

```java
var reactorHttpClient = HttpClient.create()
        .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG,
                AdvancedByteBufFormat.TEXTUAL)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

WebClient client = WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(reactorHttpClient))
    .codecs(
        clientCodecConfigurer -> {
            // use defaultCodecs() to apply the default codecs
            // clientCodecConfigurer.defaultCodecs();

            // alter a registered encoder/decoder based on the default config.
            // clientCodecConfigurer.defaultCodecs().jacksonJsonEncoder(...)

            // or use customCodecs to register codecs from scratch.
            clientCodecConfigurer.customCodecs()
                    .register(new JacksonJsonDecoder(objectMapper));
            clientCodecConfigurer.customCodecs()
                    .register(new JacksonJsonEncoder(objectMapper));
        }
    )
    .exchangeStrategies(ExchangeStrategies.withDefaults())
    .baseUrl("http://localhost:8080")
    .build();
```

## A typed client class

Rather than scattering `WebClient` calls through the application, wrap them in a dedicated client class. The sample uses `exchangeToMono`/`exchangeToFlux` to inspect the response status and convert error responses with `response.createError()`:

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

Then drive the flows in an application class:

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

You can explore the complete codes in [spring-reactive-sample/client](https://github.com/hantsy/spring-reactive-sample/tree/master/client).

## Choosing the HTTP client connector

The `ClientHttpConnector` abstracts the underlying HTTP client. By default, `WebClient` uses `ReactorClientHttpConnector` (Reactor Netty). Spring also ships connectors for other clients:

| Connector | Underlying client | Extra dependency |
| :--- | :--- | :--- |
| `ReactorClientHttpConnector` (default) | Reactor Netty | none (in WebFlux) |
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

Each connector swaps in with one builder call — `.clientConnector(connector)` — while the `PostClient` layer above stays untouched. You can explore each connector variant in [client-jetty](https://github.com/hantsy/spring-reactive-sample/tree/master/client-jetty), [client-jdk11-httpclient](https://github.com/hantsy/spring-reactive-sample/tree/master/client-jdk11-httpclient), and [client-apache-httpclient5](https://github.com/hantsy/spring-reactive-sample/tree/master/client-apache-httpclient5).

## Testing the client

The `PostClient` above is tested by stubbing the remote API with [WireMock](https://wiremock.org/) and asserting the reactive sequences — see [Mocking a target HTTP API with WireMock](../testing/web-components.md#mocking-a-target-http-api-with-wiremock) in the Testing chapter. The chapter also covers [`WebTestClient`](../testing/web-components.md) for the server side and [`StepVerifier`](../testing/intro.md#stepverifier-verify-a-publisher) for publisher-level assertions.
