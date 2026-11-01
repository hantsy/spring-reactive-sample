
# Spring Security

Spring Security is Spring's framework for protecting applications and their resources. It provides authentication (establishing who is making a request) and authorization (deciding what that identity may do), along with protections such as security headers, CSRF defenses, and session integration.

In a WebFlux application, security participates in the reactive request pipeline. A request passes through a chain of web filters before it reaches a handler. Spring Security installs its own `WebFilter` integration in that chain; the integration delegates to a `SecurityWebFilterChain` that applies authentication and authorization rules. A request that is not authenticated or authorized is rejected or redirected before the application handler runs.

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

## Security in WebFlux

The servlet stack uses servlet filters and `HttpSecurity`; WebFlux uses `WebFilter` and `ServerHttpSecurity`. The concepts are familiar, but the APIs and execution model differ. Reactive security works with the non-blocking server pipeline and reactive types rather than adapting servlet APIs into a WebFlux application.

Security also depends on the application's authentication model. A server-rendered site may use a session and form login, while an API may use bearer tokens. CSRF protection is important for browser requests that automatically attach credentials, such as session cookies; do not disable it without considering how clients authenticate and send requests.

This chapter introduces Spring Security's WebFlux support and shows how to configure these protections without leaving the non-blocking architecture.

## A friendly reading path

A natural way through this section is to begin with the general security model and then move into the more concrete configuration pieces:

- start with [An Introduction to Spring Security](start.md) to understand how the reactive model differs from the servlet-based approach
- continue with [Configure Spring Security](config.md) to see the core `ServerHttpSecurity` setup and how it behaves in practice
- then read [Configure Spring Security in Spring Boot](config-boot.md) for the auto-configuration-based variant you will usually reach for in real projects
- move into [Method level constraints](method.md) to protect business logic and endpoints with authorization rules
- finish with [Customize UserDetailsRepository](user-details-repo.md) and [Form based authentication](form.md) for user management and login flows

The chapters are ordered to build from concept to implementation: first the model, then the configuration, then the practical details that make security feel real in a reactive application.
