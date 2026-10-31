
# Spring Session (Reactive)

This chapter describes session management for reactive applications. Unlike traditional servlet sessions, reactive session support uses non-blocking stores and integration points that work with the WebFlux runtime.

## WebSession: the reactive session

WebFlux does not use the Servlet `HttpSession`. Instead, every request can access a `WebSession` — a reactive abstraction whose attributes live on the server and whose id is exchanged with the client:

```java
@Controller
public class SessionController {

    @PostMapping("/session")
    public String setAttribute(@ModelAttribute SessionAttributeForm sessionAttributeForm, WebSession session) {
        session.getAttributes().put(sessionAttributeForm.getAttributeName(), sessionAttributeForm.getAttributeValue());
        return "redirect:/";
    }

    @GetMapping("/")
    public String index(Model model, WebSession webSession) {
        model.addAttribute("webSession", webSession);
        return "index";
    }
}
```

Inject a `WebSession` as a controller method parameter and read or write attributes like a plain map. The session id is returned to the client in a `SESSION` cookie by default — there is no `JSESSIONID` cookie in the reactive stack.

## Spring Session for WebFlux

[Spring Session](https://spring.io/projects/spring-session) replaces the default in-memory session store with a pluggable `ReactiveSessionRepository`. For WebFlux applications, activate it with `@EnableSpringWebSession` (and store-specific variants such as `@EnableRedisWebSession` or `@EnableMongoWebSession`) and provide a `ReactiveSessionRepository` bean.

| Store | Annotation | Repository | Extra dependency |
| :--- | :--- | :--- | :--- |
| In-memory map | `@EnableSpringWebSession` | `ReactiveMapSessionRepository` | `spring-session-core` |
| Redis | `@EnableRedisWebSession` | `ReactiveRedisSessionRepository` (auto) | `spring-session-data-redis` |
| MongoDB | `@EnableMongoWebSession` | `ReactiveMongoOperationsSessionRepository` (auto) | `spring-session-data-mongodb` |

An external store is recommended for production: sessions survive restarts and are shared across instances.

## Choosing a session strategy

- **Server-side sessions** (this chapter) — simplest to reason about; pairs naturally with form login and HTTP Basic; use Redis or Mongo for clustered deployments.
- **Token strategies** (JWT, OAuth2) — stateless; consider when the API is consumed by mobile apps or other services.

## Chapter roadmap

1. [In-memory sessions](in-memory.md) — `@EnableSpringWebSession` with a `ReactiveMapSessionRepository`
2. [Redis-backed sessions](redis.md) — `@EnableRedisWebSession` with Lettuce
3. [Mongo-backed sessions](mongo.md) — `@EnableMongoWebSession` with the reactive Mongo driver
4. [Header-based session id](header.md) — a custom `WebSessionIdResolver` for API clients

Familiarity with [Spring Security](../security/intro.md) is helpful — the samples combine Spring Session with security configurations.
