---
sort: 7
---

# Server Sent Events

SSE (Server Sent Events) provides one-way server-to-client communications, it is  based on the existing HTTP protocol.  In the real world applications, it is useful for sending notifications to clients or some like real time events update in a stream approach, such as news update or stock update, etc.

## Server Side

In the server side, expose a  SSE endpoint is easy. Set the content type to `text/event-stream`,  and produces a `Flux` stream in the response body.

```java
@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Post> sse() {
    return Flux
        .zip(Flux.interval(Duration.ofSeconds(1)), this.posts.findAll().repeat())
        .map(Tuple2::getT2);
}
```

The above example is emitting a time based event.

Source codes: [spring-reactive-sample/sse](https://github.com/hantsy/spring-reactive-sample/blob/master/sse).  

## Client Side

There is no special client to consume SSE APIs,  like interacting with RESTful APIs,  use the existing `WebClient` to consume a SSE endpoint, do not forget to set `Accept` header to  `text/event-stream` to consume SSE events.

```java
client.get()
    .uri("messages")
    .accept(MediaType.TEXT_EVENT_STREAM)
    .retrieve()
    .bodyToFlux(Message::class.java)
```

A more close to the real world example, go to [hantsy/angular-spring-sse-sample/](https://github.com/hantsy/angular-spring-sse-sample/) in which the client is built with Angular and  the server side is a simple Spring Boot application that uses SSE to update the chat messages to client.

