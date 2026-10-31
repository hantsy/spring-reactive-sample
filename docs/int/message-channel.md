
# Reactive Message Channels

A *message channel* decouples producers from consumers: the producer hands off a message and does not know who processes it. In a reactive application, the channel itself is a stream — subscribers receive messages with backpressure, and multiple consumers can share the same source. This page compares three reactive channel mechanisms available in the Spring ecosystem, all exercised by the samples in this repository.

| Mechanism | Module | Scope | Best for |
| :--- | :--- | :--- | :--- |
| `FluxMessageChannel` | Spring Integration | In-JVM, per application context | Integration flows, HTTP in/out pipelines |
| `ReactiveRedisMessageListenerContainer` | Spring Data Redis | Distributed (Redis pub/sub) | Cross-process broadcast, lightweight messaging |
| `Sinks.Many` | Reactor | In-JVM, per stream | In-process fan-out, SSE/WebSocket feeds |

## FluxMessageChannel (Spring Integration)

`FluxMessageChannel` implements both `MessageChannel` and `Publisher<Message<?>>`: producers call `send()`, consumers subscribe as to any `Publisher`. Declare it directly, or let the [IntegrationFlow DSL](dsl.md) create one:

```java
@Bean
public MessageChannel fluxResultChannel() {
    return new FluxMessageChannel();
}
```

The [Spring Integration chapter](webflux.md) wires it between a `WebFluxInboundEndpoint` and a `@ServiceActivator` — a complete HTTP-to-HTTP reactive pipeline with backpressure from the HTTP response all the way back to the inbound request.

## Redis pub/sub (Spring Data Redis)

Redis channels work across processes: any application subscribed to the channel receives messages published by any other. Spring Data Redis exposes this through `ReactiveRedisMessageListenerContainer`, which returns a `Flux` of received messages — see [Spring Data Redis — Redis Messaging](../data/redis.md#redis-messaging) for the full walkthrough:

```java
@Bean
public ReactiveRedisMessageListenerContainer redisMessageListenerContainer(PostRepository posts, ReactiveRedisConnectionFactory connectionFactory) {
    ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(connectionFactory);
    container.receive(ChannelTopic.of("posts"))
        .map(p -> p.getMessage())
        // deserialize and persist each incoming message
        .flatMap(p -> posts.save(p))
        .subscribe();
    return container;
}

// publishing side, from any process:
reactiveRedisOperations.convertAndSend("posts", post);
```

The subscription stays open for the lifetime of the container — a stream of messages rather than a one-shot query.

## Reactor Sinks

When all producers and consumers live in the same process, a Reactor `Sinks.Many` is the simplest channel. The [SSE example](../web/sse.md#broadcasting-with-sinks) and the [WebSocket example](../web/websocket.md) both use it as a broadcast hub:

```java
@Service
public class MessageService {

    private final Sinks.Many<Message> sinks = Sinks.many().replay().latest();

    public Flux<Message> latestMessages() {
        return sinks.asFlux();   // consumer side: subscribe from anywhere
    }

    public void send(Message m) {
        sinks.emitNext(m, EmitFailureHandler.FAIL_FAST);  // producer side
    }
}
```

The same pattern drives the reactive `@EventListener` in the [Application Events](../other/event.md) sample: the listener forwards each event into a `Sinks.Many` so any part of the application can subscribe to the event stream as a `Flux`.

## Choosing between them

- Stay **in-process** and need to plug into Integration endpoints? Use `FluxMessageChannel`.
- Stay **in-process** with a plain publisher/subscriber split? Use `Sinks.Many`.
- Need messages to cross **process or instance boundaries** without standing up a full broker? Use Redis pub/sub.
- Need durability, replay, and ordering guarantees at scale? Move to a real broker — Kafka or RabbitMQ — through their reactive clients (see the [Spring Pulsar legacy sample](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/boot-pulsar) for the pattern).
