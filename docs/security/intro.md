
# An introduction to Spring Security

Spring Security is Spring's framework for protecting applications and their resources. It provides authentication (establishing who is making a request) and authorization (deciding what that identity may do), along with protections such as security headers, CSRF defenses, and session integration.

This chapter introduces Spring Security's WebFlux support and shows how to configure these protections without leaving the non-blocking architecture.

## Security in WebFlux

In a WebFlux application, security participates in the reactive request pipeline. A request passes through a chain of web filters before it reaches a handler. Spring Security installs its own `WebFilter` integration in that chain; the integration delegates to a `SecurityWebFilterChain` that applies authentication and authorization rules. A request that is not authenticated or authorized is rejected or redirected before the application handler runs.

The servlet stack uses servlet filters and `HttpSecurity`; WebFlux uses `WebFilter` and `ServerHttpSecurity`. The concepts are familiar, but the APIs and execution model differ. The core mapping looks like this:

| Servlet stack | Reactive stack |
| :--- | :--- |
| `SecurityFilterChain` | `SecurityWebFilterChain` |
| `HttpSecurity` | `ServerHttpSecurity` |
| `FilterChain` | `WebFilterChain` |
| `UserDetailsService` | `ReactiveUserDetailsService` |
| `@EnableWebSecurity` | `@EnableWebFluxSecurity` |
| `ServletRequest`/`ServletResponse` | `ServerWebExchange` |

The underlying idea is the same: a filter chain intercepts each request, authentication resolves a principal, and authorization rules decide whether the request should pass through. The difference is that the entire flow works with reactive primitives and non-blocking exchange objects.

Security also depends on the application's authentication model. A server-rendered site may use a session and form login, while an API may use bearer tokens. CSRF protection is important for browser requests that automatically attach credentials, such as session cookies; do not disable it without considering how clients authenticate and send requests.

## Add the dependencies

The reactive pieces ship in the standard Spring Security modules — no separate reactive artifact is required. When you are developing a WebFlux application without Spring Boot, add the security dependencies like this (versioning is typically managed by `spring-boot-starter-parent`):

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

## How request security is applied

The security filter chain is configured as a Spring bean. Each `SecurityWebFilterChain` combines security filters with request-matching rules. Spring evaluates the chains in order and uses the first chain whose security matcher applies to the request. Within the selected chain, authorization rules are evaluated in declaration order, so put specific matchers before broad ones and end with an intentional catch-all rule.

For example, route-level rules can allow a public endpoint, require a role for an administrative area, and require authentication for everything else:

```java
@Bean
SecurityWebFilterChain applicationSecurity(ServerHttpSecurity http) {
    return http
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/about", "/health").permitAll()
            .pathMatchers("/admin/**").hasRole("ADMIN")
            .anyExchange().authenticated()
        )
        .build();
}
```

These rules protect requests based on their paths and other request attributes. Authentication can be provided by mechanisms such as form login, HTTP Basic, OAuth 2.0 login, or bearer tokens; choose and configure only the mechanisms that match the application.

## Method security

Path rules are a useful first boundary, but they are not always enough. A route may be available to several roles while a particular operation or object requires a more specific permission. Method security lets you express those checks close to the service method:

```java
@Configuration
@EnableReactiveMethodSecurity
class MethodSecurityConfig {
}

@Service
class ReportService {

    private final ReportRepository reports;

    ReportService(ReportRepository reports) {
        this.reports = reports;
    }

    @PreAuthorize("hasRole('ADMIN') or #owner == authentication.name")
    public Mono<Report> findReport(String owner, String reportId) {
        return reports.findById(reportId);
    }
}
```

Use the filter chain for broad request-level policy and method security for fine-grained checks around business operations. They complement one another: method security does not replace URL authorization, and URL authorization alone may not express ownership or domain-specific rules. For reactive applications, enable reactive method security and ensure secured methods are invoked through Spring-managed beans so the security interception applies.

## A friendly reading path

This page covers the general security model. A natural way through the rest of the chapter is to move into the concrete configuration pieces:

- continue with [Configure Spring Security](config.md) to see the core `ServerHttpSecurity` setup and how it behaves in practice
- then read [Configure Spring Security in Spring Boot](config-boot.md) for the auto-configuration-based variant you will usually reach for in real projects
- move into [Method level constraints](method.md) to protect business logic and endpoints with authorization rules
- finish with [Customize UserDetailsRepository](user-details-repo.md) and [Form based authentication](form.md) for user management and login flows

The chapters are ordered to build from concept to implementation: first the model, then the configuration, then the practical details that make security feel real in a reactive application.
