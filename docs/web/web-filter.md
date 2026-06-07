---
title: WebFilter
parent: Reactive Web
nav_order: 2
toc: true
---

# WebFilter

WebFilter is a core component in the reactive stack that intercepts and processes HTTP requests and responses.

The `WebFilter` interface looks like the following.

```java
public interface WebFilter {

	Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain);

}
```

The `filter` method receives a `ServerWebExchange` that lets you inspect the request and perform cross-cutting operations before or after the request is handled.

The `WebFilterChain` is similar to the role of `FilterChain` in the Servlet Filter. At runtime, a series of `WebFilter` can be chained to execute in one web request.

The following is an example of `WebFilter` where it works as a security checker. If there is no _user_ query parameter provided, then send a `UNAUTHORIZED` status code to the response.

```java
@Component
public class SecurityWebFilter implements WebFilter{
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if(!exchange.getRequest().getQueryParams().containsKey("user")){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        }
        return chain.filter(exchange);
    }
}
```

> NOTE: In production applications prefer Spring Security for authentication and authorization. This example demonstrates how a simple WebFilter can be used for small checks or prototyping.

For the complete codes, check [spring-reactive-sample/web-filter](https://github.com/hantsy/spring-reactive-sample/blob/master/web-filter).
