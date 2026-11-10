
# Method level constraints

Like traditional Spring WebMvc applications, you can use a `@PreAuthorize("hasRole('ADMIN')")` annotation on your methods to prevent the execution of a method when the expression defined in `@PreAuthorize` evaluates to false.

Path rules are a useful first boundary, but they are not always enough. A route may be open to several roles while a particular operation — or the object an operation touches — needs a more specific permission. Method security puts that check next to the business method it protects.

## Enable method security

To enable method level security, add `@EnableReactiveMethodSecurity` to your security configuration class:

```java
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                        //.pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")//replace this with method level constraints
                        .anyExchange().authenticated())
                .build();
    }

    // ...
}
```

The annotation applies to Spring-managed beans: the secured method has to be invoked through the proxy that method security creates around the bean, so a call from inside the same bean bypasses the check.

## Apply constraints on reactive methods

Annotate the methods in your business code — the sample applies the constraint on the repository:

```java
@PreAuthorize("hasRole('ADMIN')")
Mono<Post> delete(Long id) {
    Post deleted = data.get(id);
    data.remove(id);
    return Mono.just(deleted);
}
```

`@PreAuthorize` works on methods returning `Mono` or `Flux` because the reactive method security interceptor checks the expression before subscribing to the returned publisher — the decision happens before any data flows.

The expression can also reach the method's own arguments, which is how an ownership check is written:

```java
@PreAuthorize("hasRole('ADMIN') or #owner == authentication.name")
public Mono<Report> findReport(String owner, String reportId) {
    return reports.findById(reportId);
}
```

With the URL-level rules replaced by the annotation, a `DELETE /posts/{id}` call from a non-admin user is rejected with 403 before the repository method runs.

Method security and request-level authorization complement each other rather than compete: keep the broad policy in the `SecurityWebFilterChain` and put the checks that depend on the arguments or the domain object on the method, where the data needed to decide is in scope.

You can explore the complete codes in [spring-reactive-sample/security-method](https://github.com/hantsy/spring-reactive-sample/tree/master/security-method).
