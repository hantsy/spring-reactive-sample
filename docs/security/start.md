
# An Introduction to Spring Security

Reactive applications still need security, but the APIs look different from the Servlet world. In Spring Security, the reactive stack swaps the classic servlet filters and request objects for their non-blocking equivalents.

The core mapping looks like this:

| Servlet stack | Reactive stack |
| :--- | :--- |
| `SecurityFilterChain` | `SecurityWebFilterChain` |
| `HttpSecurity` | `ServerHttpSecurity` |
| `FilterChain` | `WebFilterChain` |
| `UserDetailsService` | `ReactiveUserDetailsService` |
| `@EnableWebSecurity` | `@EnableWebFluxSecurity` |
| `ServletRequest`/`ServletResponse` | `ServerWebExchange` |

The underlying idea is the same: a filter chain intercepts each request, authentication resolves a principal, and authorization rules decide whether the request should pass through. The difference is that the entire flow works with reactive primitives and non-blocking exchange objects.

The early `spring-security-webflux` artifact name is an older memory aid; the modern stack ships under the same core Spring Security modules, and the reactive pieces are part of the standard security setup.

Add the security dependencies like this (versioning is typically managed by `spring-boot-starter-parent`):

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-core</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-web</artifactId>
</dependency>
```

For Spring Boot applications, a single starter is enough. See [Configure Spring Security in Spring Boot applications](config-boot.md).
