
# RSocket

RSocket is a binary messaging protocol that runs over transports such as TCP and WebSocket. It supports four interaction models—request/response, request/stream, fire-and-forget, and channel—while Spring provides a familiar programming model for building RSocket servers and clients.

## The four interaction models

Every RSocket exchange follows one of four interaction models. Each one maps neatly onto a Reactor type:

| Model | Direction | Reactive types | Route example |
| :--- | :--- | :--- | :--- |
| Request/Response | 1 → 1 | `Mono<T>` → `Mono<R>` | `posts.findById.{id}` |
| Request/Stream | 1 → N | `Mono<T>` → `Flux<R>` | `greet-stream` |
| Fire-and-Forget | 1 → 0 | `Mono<T>` → `Mono<Void>` | `hello` |
| Channel | N → N (bidirectional) | `Flux<T>` → `Flux<R>` | `greet-channel` |

## RSsocket Java API

Before jumping into Spring wrapper, it is worth looking at the plain [rsocket-java](https://github.com/rsocket/rsocket-java) API. 

The following code snippet demonstrates how to start an RSocket server and build a client to interact with it.

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

The types come directly from rsocket-java:

* `io.rsocket.core.RSocketServer.create()` starts an RSocket server.
* `io.rsocket.core.RSocketConnector.create()` starts an RSocket client.

 Every interaction model follows the same pattern (`requestResponse`, `requestStream`, `fireAndForget`, and `requestChannel` on the `RSocket` interface). 

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
