
# Method level constraints

Like traditional Spring WebMvc applications, you can use a `@PreAuthorize("hasRole('ADMIN')")` annotation on your methods to prevent the execution of a method when the expression defined in `@PreAuthorize` evaluates to false.

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

With the URL-level rules replaced by the annotation, a `DELETE /posts/{id}` call from a non-admin user is rejected with 403 before the repository method runs.

For the complete codes, check [spring-reactive-sample/security-method](https://github.com/hantsy/spring-reactive-sample/tree/master/security-method).
