
# RSocket

RSocket is a protocol for multiplexed, binary messaging over TCP, WebSocket, and other transports. Unlike HTTP's request/response flow, it supports four interaction models natively, and Spring gives them a natural programming model: `spring-boot-starter-rsocket` starts the server, `@MessageMapping` exposes routes, and `RSocketRequester` calls remote endpoints from the client.

This chapter brings the examples from the companion [hantsy/rsocket-sample](https://github.com/hantsy/rsocket-sample) project into smaller, runnable modules inside this repository.

## The four interaction models

Every RSocket exchange follows one of four interaction models. Each one maps neatly onto a Reactor type:

| Model | Direction | Reactive types | Route example |
| :--- | :--- | :--- | :--- |
| Request/Response | 1 → 1 | `Mono<T>` → `Mono<R>` | `posts.findById.{id}` |
| Request/Stream | 1 → N | `Mono<T>` → `Flux<R>` | `greet-stream` |
| Fire-and-Forget | 1 → 0 | `Mono<T>` → `Mono<Void>` | `hello` |
| Channel | N → N (bidirectional) | `Flux<T>` → `Flux<R>` | `greet-channel` |

## The raw rsocket-java API

Before Spring wraps it for us, it is worth looking at the plain [rsocket-java](https://github.com/rsocket/rsocket-java) API. The [vanilla](https://github.com/hantsy/rsocket-sample/tree/master/vanilla) example in the companion repository runs a raw server and client side by side: the server accepts connections and echoes payloads back to the caller.

```java
final RSocket responseHandler = new RSocket() {
    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        return Mono.just(DefaultPayload.create("received (" + payload.getDataUtf8() + ") at " + Instant.now()));
    }
};

// server side: bind a socket acceptor to a TCP transport
RSocketServer.create(
        (setupPayload, reactiveSocket) -> Mono.just(responseHandler)
)
        .bind(TcpServerTransport.create("localhost", PORT))
        .block();

// client side: connect and exchange payloads
RSocket socket = RSocketConnector.create()
        .connect(TcpClientTransport.create("localhost", PORT))
        .block();

socket.requestResponse(DefaultPayload.create("Hello"))
        .subscribe(r -> System.out.println(r.getDataUtf8()));
```

The types come directly from rsocket-java — `io.rsocket.core.RSocketServer`, `io.rsocket.core.RSocketConnector`, and `io.rsocket.transport.netty.*`. Every interaction model follows the same pattern (`requestResponse`, `requestStream`, `fireAndForget`, and `requestChannel` on the `RSocket` interface). The vanilla sample includes complete Request/Response, Request/Stream, Fire-and-Forget, and Request/Channel examples.

## Spring's RSocket support

Spring wraps the same protocol with a familiar programming model:

- **`spring-boot-starter-rsocket`** — starts an RSocket server over TCP or WebSocket, configures `RSocketStrategies` (including JSON codecs), and provides an `RSocketRequester.Builder` for client calls.
- **`@MessageMapping`** — exposes incoming routes in a controller, much like `@GetMapping` exposes HTTP endpoints. `@DestinationVariable` captures route parameters.
- **`RSocketRequester`** — the client-side fluent API: `route(...)`, `data(...)`, then `send()` for fire-and-forget calls, `retrieveMono(...)` for a single reply, `retrieveFlux(...)` for streams or channels, or `data(Flux...)` + `retrieveFlux(...)` for bidirectional channel traffic.

## A practical reading path

1. [RSocket over TCP](tcp.md) — the four interaction models end to end over a TCP connection.
2. [RSocket over WebSocket](websocket.md) — the same handlers exposed over WebSocket with a mapping path.
3. [Building a CRUD application](crud.md) — RSocket messaging backed by Spring Data R2DBC in a more realistic application.
4. [RSocket with Spring Integration](integration.md) — IntegrationFlow-based inbound and outbound RSocket gateways.
