
# The JDK Flow API

The [`java.util.concurrent.Flow`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Flow.html) API packages the Reactive Streams interfaces inside the JDK (`Flow.Publisher`, `Flow.Subscriber`, `Flow.Subscription`, `Flow.Processor`) and adds a ready-made `Flow.Publisher` implementation: `SubmissionPublisher`.

Spring WebFlux provides adapter support for it, so a `Flow.Publisher` can be a controller return type or parameter.

## Flow.Publisher as a return type

The [java9](https://github.com/hantsy/spring-reactive-sample/tree/master/java9) sample returns a `Flow.Publisher` from a controller and bridges Reactor's `Flux` with `JdkFlowAdapter`:

```java
@RestController
@RequestMapping(value = "/posts")
public class PostController {

    @GetMapping
    public Flow.Publisher<Post> all() {
        return JdkFlowAdapter.publisherToFlowPublisher(
                Flux.just(
                        new Post(1L, "post one", "content of post one"),
                        new Post(2L, "post two", "content of post two")
                )
        );
    }
}
```

Spring accepts the `Flow.Publisher` and drives it through the reactive request handling stack — the response is streamed exactly as with a `Flux` return type.

## The JDK HttpClient

The JDK ships a reactive `java.net.http.HttpClient` built on the Flow API. When you need a JDK-native client outside of Spring, it frees you from third-party HTTP client libraries:

```java
HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8080/posts"))
        .build();

client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenAccept(System.out::println)
        .join();
```

Inside a Spring application, prefer [WebClient with the JDK connector](../web/client.md#jdk-httpclient), which wraps the same `HttpClient` behind the `ClientHttpConnector` abstraction.

For the complete codes, check [spring-reactive-sample/java9](https://github.com/hantsy/spring-reactive-sample/tree/master/java9).
