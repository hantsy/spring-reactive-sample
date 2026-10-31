
# WebSocket

WebSocket is a bi-directional, multiplexed protocol built on HTTP. It is widely used for real-time client-server communication — chat, live updates, gaming — and supports custom subprotocols.

Spring WebFlux adds simple WebSocket support built on reactive types: a `WebSocketHandler` processes inbound and outbound streams of messages, and a `HandlerMapping` registers handlers at URLs.

## Server side

### Step 1: implement a WebSocketHandler

Create a handler to process the incoming messages and send a response back to the client. The simplest form is an echo handler that loops the inbound stream back to the outbound stream:

```java
public class EchoHandler implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(session.receive()
                .doOnNext(WebSocketMessage::retain) // Use retain() for Reactor Netty
                .map(m -> session.textMessage("received:" + m.getPayloadAsText()))
        );
    }
}
```

`WebSocketSession` exposes `receive()` and `send(Publisher<WebSocketMessage>)` as reactive streams, so a handler is just a composition of the two flows.

A more realistic handler maintains a shared `Sinks.Many` and broadcasts every received message to all connected sessions — the same hub pattern as the [SSE example](sse.md#broadcasting-with-sinks):

```java
@RequiredArgsConstructor
public class MessagetHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;

    private Sinks.Many<Message> sinks = Sinks.many().replay().limit(2);
    private Flux<Message> outputMessages = sinks.asFlux();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        var receiveMono = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::readIncomingMessage)
                .flatMap(req -> Mono.fromCallable(
                        () -> new Message(UUID.randomUUID(), req.message(), LocalDateTime.now())))
                .log("server receiving::")
                .doOnNext(data -> executor.execute(
                        () -> sinks.emitNext(data, Sinks.EmitFailureHandler.FAIL_FAST)))
                .doOnError(error -> sinks.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST))
                .then();

        var sendMono = session
                .send(
                        Mono.delay(Duration.ofMillis(500))
                                .thenMany(outputMessages.map(msg -> session.textMessage(toJsonString(msg)))))
                .log("server sending::")
                .onErrorResume(throwable -> session.close())
                .then();

        return Mono.zip(receiveMono, sendMono).then();
    }

    // toJsonString(...) and readIncomingMessage(...) convert to/from JSON
}
```

### Step 2: register the handler in a HandlerMapping

```java
@Configuration
@EnableWebFlux
class WebConfig {

    @Bean
    public HandlerMapping handlerMapping(ObjectMapper objectMapper) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/echo", new EchoHandler());
        map.put("/ws/messages", new MessagetHandler(objectMapper));

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
```

In a non-Spring Boot application, you have to declare the `WebSocketHandlerAdapter` bean explicitly. Spring Boot auto-configures it when `spring-boot-starter-webflux` is on the classpath.

Source codes: [spring-reactive-sample/websocket](https://github.com/hantsy/spring-reactive-sample/tree/master/websocket).

## Client side

Spring provides a built-in Reactor Netty based WebSocket client to communicate with the server side.

The following test fragment connects to `/echo`, sends 100 messages, and collects the echoed responses:

```java
WebSocketClient client = new ReactorNettyWebSocketClient();

int count = 100;
Flux<String> input = Flux.range(1, count).map(index -> "msg-" + index);
ReplayProcessor<Object> output = ReplayProcessor.create(count);

client.execute(new URI("ws://localhost:8080/echo"),
               session -> {
                   log.debug("Starting to send messages");
                   return session
                       .send(input.doOnNext(s -> log.debug("outbound " + s)).map(session::textMessage))
                       .thenMany(session.receive().take(count).map(WebSocketMessage::getPayloadAsText))
                       .subscribeWith(output)
                       .doOnNext(s -> log.debug("inbound " + s))
                       .then()
                       .doOnTerminate((aVoid, ex) ->
                               log.debug("Done with " + (ex != null ? ex.getMessage() : "success")));
               })
    .block(Duration.ofMillis(5000));
```

Source codes: [spring-reactive-sample/client](https://github.com/hantsy/spring-reactive-sample/tree/master/client).

For a complete real world example application, see [hantsy/angular-spring-websocket-sample](https://github.com/hantsy/angular-spring-websocket-sample/), which combines a Mongo tailable cursor, WebFlux, and an Angular client.

## WebSocket vs SSE

| Requirement | Use |
| :--- | :--- |
| Server → client only (notifications, feeds) | [Server Sent Events](sse.md) — simpler, plain HTTP, auto-reconnect |
| Bi-directional (chat, collaborative editing) | WebSocket |
| Request/response with backpressure over the network | RSocket — see [hantsy/rsocket-sample](https://github.com/hantsy/rsocket-sample) |
