
# Spring Session (Reactive)

Session management in a reactive application is not just a small variant of the servlet model. WebFlux introduces a different runtime model, so the session abstraction is also different: it is built around `WebSession`, non-blocking repositories, and integrations that work with the reactive pipeline.

This chapter shows how to use Spring Session in a WebFlux application without falling back to blocking servlet assumptions.

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

## A friendly reading path

The session chapter is easiest to read in the same order you would apply it in a real application:

- start with [In-memory sessions](in-memory.md) to see the basic reactive session model without adding external infrastructure
- then read [Redis-backed sessions](redis.md) when you want a production-friendly, shared session store
- continue with [Mongo-backed sessions](mongo.md) for a document-store option that also fits a reactive ecosystem
- finish with [Header-based session id](header.md) for API-first clients that do not want to rely on cookies

If you are already working with [Spring Security](../../security/intro.md), the examples in this section will feel especially relevant because they fit directly into the authenticated request flow.
