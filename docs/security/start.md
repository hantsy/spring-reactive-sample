
# An Introduction to Spring Security

To align with the reactive WebFlux programming model, Spring Security introduced reactive support (`spring-security-web`, `spring-security-config`). The reactive stack is no longer a separate artifact: since Spring Security 5.x the reactive classes ship inside the same core modules the servlet stack uses, so `spring-security-webflux` — the name you may remember from the early Spring Security 5 milestones — no longer exists.

At runtime, the reactive stack swaps every Servlet-era primitive for a reactive counterpart:

| Servlet stack | Reactive stack |
| :--- | :--- |
| `SecurityFilterChain` | `SecurityWebFilterChain` |
| `HttpSecurity` | `ServerHttpSecurity` |
| `FilterChain` | `WebFilterChain` |
| `UserDetailsService` | `ReactiveUserDetailsService` |
| `@EnableWebSecurity` | `@EnableWebFluxSecurity` |
| `ServletRequest`/`ServletResponse` | `ServerWebExchange` |

The programming model is otherwise the same: a security filter chain intercepts exchanges, an authentication manager resolves credentials against a `ReactiveUserDetailsService`, and authorization rules apply per path pattern or per method.

Include the dependencies (versions are managed by `spring-boot-starter-parent`):

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

For Spring Boot applications, a single starter is enough — see [Configure Spring Security in Spring Boot applications](config-boot.md).
