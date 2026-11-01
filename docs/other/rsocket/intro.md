
# RSocket

RSocket is an application protocol for multiplexed, binary messaging over TCP, WebSocket, and other transports. Unlike HTTP's request/response model, RSocket supports four interaction models natively — and Spring embraces it fully: `spring-boot-starter-rsocket` starts an RSocket server, `@MessageMapping` handles incoming routes, and `RSocketRequester` calls remote routes from the client side.

This chapter ports the samples of the companion [hantsy/rsocket-sample](https://github.com/hantsy/rsocket-sample) repository into small, runnable modules of this repository.

## The four interaction models

Every RSocket exchange uses one of four models, each mapping onto a Reactor type:

| Model | Direction | Reactive types | Route example |
| :--- | :--- | :--- | :--- |
| Request/Response | 1 → 1 | `Mono<T>` → `Mono<R>` | `posts.findById.{id}` |
| Request/Stream | 1 → N | `Mono<T>` → `Flux<R>` | `greet-stream` |
| Fire-and-Forget | 1 → 0 | `Mono<T>` → `Mono<Void>` | `hello` |
| Channel | N → N (bidirectional) | `Flux<T>` → `Flux<R>` | `greet-channel` |

## The raw rsocket-java API

Before Spring's wrappers, it is worth seeing the plain [rsocket-java](https://github.com/rsocket/rsocket-java) API. The [vanilla](https://github.com/hantsy/rsocket-sample/tree/master/vanilla) sample of the companion repository runs a raw server and client side by side — a server that accepts connections and echoes payloads:

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

The types come from rsocket-java directly — `io.rsocket.core.RSocketServer`, `io.rsocket.core.RSocketConnector`, `io.rsocket.transport.netty.*` — and every interaction model follows the same shape (`requestResponse`, `requestStream`, `fireAndForget`, `requestChannel` on the `RSocket` interface). The vanilla sample includes the complete Request/Response, Request/Stream, Fire-and-Forget, and Request/Channel examples.

## Spring's RSocket support

Spring wraps the same protocol with its programming model:

- **`spring-boot-starter-rsocket`** — starts an RSocket server (TCP or WebSocket transport, configured by properties) and auto-configures `RSocketStrategies` (JSON codecs included) and an `RSocketRequester.Builder` for the client side.
- **`@MessageMapping`** — handles incoming routes, in a controller just like `@GetMapping` for HTTP. `@DestinationVariable` captures route variables.
- **`RSocketRequester`** — the client-side fluent API: `route(...)`, `data(...)`, then `send()` (fire-and-forget), `retrieveMono(...)` (response), `retrieveFlux(...)` (stream), or `data(Flux...)` + `retrieveFlux(...)` (channel).

## Chapter roadmap

1. [RSocket over TCP](tcp.md) — the four interaction models end to end over a TCP transport.
2. [RSocket over WebSocket](websocket.md) — the same handlers served over WebSocket with a mapping path.
3. [Building a CRUD application](crud.md) — RSocket messaging backed by Spring Data R2DBC, closer to a real application.
4. [RSocket with Spring Integration](integration.md) — the IntegrationFlow DSL with RSocket inbound and outbound gateways.
