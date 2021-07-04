---
sort: 8
---

# WebSocket
WebSocket is a bi-directional multiplexed protocol,  it is based on HTTP protocol.  WebSocket is wildly used in client and server real-time communications, such as online gaming,  and multiple clients chat applications.   It is very flexible to create your own sub protocol. 

Spring WebFlux adds simple basic WebSocket support.

## Server Side

Firstly create a WebSocket Handler to process the incoming messages and send back to the client.

```java
public class PostsWebSocketHandler implements WebSocketHandler {

    private final PostRepository posts;

    public PostsWebSocketHandler(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public List<String> getSubProtocols() {
        return Arrays.asList("test");
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String protocol = session.getHandshakeInfo().getSubProtocol();
        WebSocketMessage message = session.textMessage(this.posts.findAll().takeLast(0).toString());
        return doSend(session, Mono.just(message));
    }

    // TODO: workaround for suspected RxNetty WebSocket client issue
    // https://github.com/ReactiveX/RxNetty/issues/560
    private Mono<Void> doSend(WebSocketSession session, Publisher<WebSocketMessage> output) {
        return session.send(Mono.delay(Duration.ofMillis(100)).thenMany(output));
    }

}
```

And register this  handler in a `HandlerMapping` bean. 

```java
@Bean
public HandlerMapping handlerMapping() {
    Map<String, WebSocketHandler> map = new HashMap<>();
    map.put("/echo", new EchoWebSocketHandler());
    map.put("/posts", new PostsWebSocketHandler(this.posts));

    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
    mapping.setUrlMap(map);
    return mapping;
}
```

In a none Spring Boot application,  you have to declare a `WebSocketHandlerAdapter` bean.

```java
@Bean
WebSocketHandlerAdapter webSocketHandlerAdapter(){
    return new WebSocketHandlerAdapter();
}
```

Source codes:  [spring-reactive-sample/websocket](https://github.com/hantsy/spring-reactive-sample/blob/master/websocket).

## Client Side

Let's move to the client side to interact with this WebSocket.

Spring provides a built-in Netty based WebSocket client to communicate with the server side.

```java
WebSocketClient client = new ReactorNettyWebSocketClient();
//        client.execute(new URI("ws://localhost:8080/echo"), (WebSocketSession session) -> {
//            session.send().log().;
//        });

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

}
```

Source codes: [spring-reactive-sample/client](https://github.com/hantsy/spring-reactive-sample/blob/master/client).

A more close to the real world example application, go to [hantsy/angular-spring-websocket-sample/](https://github.com/hantsy/angular-spring-websocket-sample/).