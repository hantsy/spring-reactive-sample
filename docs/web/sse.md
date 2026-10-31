
# Server Sent Events

SSE (Server Sent Events) provides one-way server-to-client communication over the existing HTTP protocol. In real world applications, it is useful for sending notifications to clients or pushing near real-time updates in a stream — news feeds, stock prices, or chat updates.

In WebFlux, an SSE endpoint is a controller method (or a functional route) that produces a `Flux` with the `text/event-stream` content type. The stream can come from anywhere — a timer, a database tailable cursor, or a `Sinks` hub that broadcasts messages as they arrive.

## Server side

### Emitting a stream from a timer

Set the content type to `text/event-stream` and return a `Flux`. The following example emits a time-paced event:

```java
@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Post> sse() {
    return Flux
        .zip(Flux.interval(Duration.ofSeconds(1)), this.posts.findAll().repeat())
        .map(Tuple2::getT2);
}
```

### Broadcasting with Sinks

For an application where clients push messages and all connected clients receive them, a Reactor `Sinks.Many` acts as a hub: the service exposes the sink as a `Flux` for subscribers, and pushes new messages into it as they arrive.

```java
@Service
public class MessageService {

    private final Sinks.Many<Message> sinks = Sinks.many().replay().latest();

    public Flux<Message> latestMessages() {
        return sinks.asFlux();
    }

    public void send(Message m) {
        sinks.emitNext(m, EmitFailureHandler.FAIL_FAST);
    }

}
```

The `replay().latest()` configuration replays the most recent message to a new subscriber, so a client reconnecting to the stream sees the latest state.

Expose the stream with a controller:

```java
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> messageStream() {
        return this.messageService.latestMessages();
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> send(@RequestBody MessageRequest request) {
        var message = Message.builder()
            .id(UUID.randomUUID())
            .body(request.message())
            .sentAt(LocalDateTime.now())
            .build();
        this.messageService.send(message);
        return Mono.just(created(URI.create("/messages/" + message.getId())).build());
    }
}
```

Every `POST /messages` fans out to all SSE subscribers of `GET /messages`.

Source codes: [spring-reactive-sample/sse](https://github.com/hantsy/spring-reactive-sample/tree/master/sse).

## Client side

There is no special client for SSE. Like interacting with RESTful APIs, use the existing `WebClient` to consume an SSE endpoint, and set the `Accept` header to `text/event-stream`:

```java
client.get()
    .uri("messages")
    .accept(MediaType.TEXT_EVENT_STREAM)
    .retrieve()
    .bodyToFlux(Message.class)
    .subscribe(System.out::println);
```

In a browser, the built-in `EventSource` API consumes the same endpoint.

For a complete real world application, see [hantsy/angular-spring-sse-sample](https://github.com/hantsy/angular-spring-sse-sample/), in which the client is built with Angular and the server side is a simple Spring Boot application that uses SSE to broadcast chat messages — backed by a MongoDB tailable cursor, see [Tailable Query](../data/data-mongo.md#tailable-query).
