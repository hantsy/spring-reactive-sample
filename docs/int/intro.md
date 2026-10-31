
# Reactive Streams and Spring Integration

Spring Integration extends the Spring programming model into messaging and enterprise integration patterns — channels, endpoints, gateways, and adapters. Since Spring Integration 5, the framework participates in the reactive world: message channels can be backed by Reactor `Flux`es, inbound endpoints can serve HTTP through WebFlux, and outbound handlers are driven by `WebClient`.

This chapter shows how the pieces fit together, using two runnable samples:

- [WebFlux endpoints with Spring Integration](webflux.md) — an HTTP inbound endpoint wired to a reactive channel and a reactive outbound handler, assembled with annotations.
- [Spring Integration Java DSL](dsl.md) — the same flow assembled with the fluent `IntegrationFlow` DSL.
- [Reactive Message Channels](message-channel.md) — reactive message channels across the ecosystem: Spring Integration's `FluxMessageChannel`, Redis pub/sub, and Reactor `Sinks`.

## How reactive support works

Spring Integration introduces a reactive channel type, `FluxMessageChannel`, which implements both `MessageChannel` and `Publisher<Message<?>>`. Producers send messages to it through the ordinary `send()` contract; consumers subscribe to it as a Reactive Streams `Publisher` and get backpressure for free.

On the web side, two components bridge Integration to WebFlux:

- `WebFluxInboundEndpoint` — receives HTTP requests, maps them to Integration messages, and writes the (possibly streamed) reply back to the response.
- `WebFluxRequestExecutingMessageHandler` — the outbound counterpart; it executes HTTP requests through a `WebClient` and produces reactive replies.

Together they form a full reactive processing pipeline: HTTP in → message channel → HTTP out, without a blocking call anywhere.

## How the chapter's examples are structured

Both samples share the same skeleton — a WebFlux app serving `GET /posts` from an in-memory repository — and add an Integration layer on top that exposes `GET /all`. A request to `/all` travels through the Integration flow, which calls back into the same app's `/posts` endpoint through a `WebClient`, and streams the result to the caller.

```
GET /all ──▶ WebFluxInboundEndpoint ──▶ FluxMessageChannel ──▶ WebFluxRequestExecutingMessageHandler ──▶ GET /posts
```

The samples are plain Spring applications (no Spring Boot auto-configuration) built with the manual wiring from the [Getting Started](../start/intro.md) chapter, with one extra dependency:

```xml
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-webflux</artifactId>
</dependency>
```

## Where to go next

- [WebFlux endpoints with Spring Integration](webflux.md) — annotation-based assembly.
- [Spring Integration Java DSL](dsl.md) — the same flow with `IntegrationFlow.from(...)`.
- [Reactive Message Channels](message-channel.md) — channel patterns across Spring Integration, Spring Data Redis, and Reactor.
