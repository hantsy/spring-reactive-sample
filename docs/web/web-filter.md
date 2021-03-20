---
sort: 2
---

# WebFilter

One of the most important components in Reactive Stack is the `WebFilter` which is used to handle web requests from HTTP client.

The `WebFilter` interface looks like the following.

```java
public interface WebFilter {

	Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain);

}
```

The `filter` method accepts a `ServerWebExchange` where you can interact with web request and do crossing-cut operations as you expected in the response.

The `WebFilterChain` is similar to the  role of  `FilterChain` in the Servlet Filter.  At runtime, a series of  `WebFilter` can be chained to execute in one web request.

The following is an example of `WebFilter` where  it works as a security checker. If there is no *user* query parameter provided, then send a `UNAUTHORIZED` status code to the response.

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

> NOTE: In a Spring application, I would like use Spring Security to handle security considerations. Here I just use this as an example of `WebFitler`.
> 


For the complete codes, check [spring-reactive-sample/web-filter](https://github.com/hantsy/spring-reactive-sample/blob/master/web-filter).