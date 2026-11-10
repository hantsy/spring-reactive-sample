
# Configure Spring Security

When you are developing a Spring WebFlux application without Spring Boot, add the security dependencies from the [previous page](intro.md) to your project.

Create a configuration class and add the `@EnableWebFluxSecurity` annotation to enable WebFlux support for Spring Security:

```java
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
            .authorizeExchange(exchange -> exchange
                .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                .anyExchange().authenticated())
            .build();
    }

    private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
            .map(a -> context.getVariables().get("user").equals(a.getName()))
            .map(AuthorizationDecision::new);
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsRepository() {
        UserBuilder users = User.withDefaultPasswordEncoder();
        UserDetails user = users.username("user").password("password").roles("USER").build();
        UserDetails admin = users.username("admin").password("password").roles("USER", "ADMIN").build();
        return new MapReactiveUserDetailsService(user, admin);
    }
}
```

In the above codes:

1. The `@EnableWebFluxSecurity` annotation enables Security for a `spring-webflux` based application.
2. A `SecurityWebFilterChain` bean is required to configure the details of Spring Security. `ServerHttpSecurity` comes from the reactive config support — similar to the servlet-stack `HttpSecurity`, but it handles `ServerWebExchange` instead of the Servlet based `WebRequest`.
3. A `ReactiveUserDetailsService` resolves users reactively. The in-memory `MapReactiveUserDetailsService` is the default choice for demos; you can customize it by implementing the interface yourself — see [Customize UserDetailsRepository](user-details-repo.md).
4. The `pathMatchers` rules protect the API: `GET /posts/**` is public, `DELETE /posts/**` requires the `ADMIN` role, and every other exchange must be authenticated.

## Verify with curl

Start up the application and verify that the Spring Security configuration works as expected.

After it is started, try to add a new post without authentication:

```
#curl -v -X POST http://localhost:8080/posts -H "Content-Type:application/json" -d "{\"title\":\"My Post\",\"content\":\"content of My Post\"}"
< HTTP/1.1 401 Unauthorized
< WWW-Authenticate: Basic realm="Realm"
```

The server side rejects the client request and sends back a 401 error (401 Unauthorized).

Use the predefined `user`/`password` credentials to get authenticated and send the request again:

```
curl -v -X POST http://localhost:8080/posts -u "user:password" -H "Content-Type:application/json" -d "{\"title\":\"My Post\",\"content\":\"content of My Post\"}"
< HTTP/1.1 200 OK
< set-cookie: SESSION=b99124f7-c0a0-4507-b9be-34718af3d137; HTTPOnly
<
{"id":"59906f9d3c44060e044fb378","title":"My Post","content":"content of My Post"}
```

The request succeeds and returns the newly created post.

## Restricting access by path variable

The built-in rules — `permitAll()`, `hasRole()`, `authenticated()` — are static: they say *who* may call an endpoint, but not *whose data* the caller may touch. To express a rule like "a user may only access their own resource", replace the static rule with a custom decision via `access(...)`.

The [security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo) sample demonstrates this against a Mongo-backed user store. It protects `GET /users/{username}` so that only the user themselves can read it:

```java
@Bean
SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(csrf -> csrf.disable())
        .httpBasic(withDefaults())
        .authorizeExchange(exchange -> exchange
            .pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
            .anyExchange().authenticated())
        .build();
}
```

`access(...)` accepts a `ReactiveAuthorizationManager<AuthorizationContext>`, which is a functional interface — a method reference fits its single `authorize` method:

```java
private Mono<AuthorizationResult> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
    return authentication
            .map(a -> context.getVariables().get("user").equals(a.getName()))
            .map(granted -> (AuthorizationResult) new AuthorizationDecision(granted));
}
```

Walking through the decision:

1. `Mono<Authentication>` supplies the authenticated principal, resolved reactively from the security context. For an anonymous request it is empty: the comparison never runs, no decision is granted, and the authentication entry point rejects the request with `401`.
2. `AuthorizationContext` wraps the current `ServerWebExchange` together with the URI template variables of the matched `PathPattern`. Since the rule is registered for `/users/{user}/**`, `context.getVariables().get("user")` is the `{user}` segment of the request path — e.g. `"admin"` for `GET /users/admin`. The trailing `/**` also matches zero segments, so `/users/user` itself is covered.
3. The decision compares the path variable with `Authentication.getName()` (the username) and wraps the boolean in an `AuthorizationDecision`. `true` lets the request continue to the controller; `false` ends it with `403 Forbidden`.

Two details are worth noting:

- In Spring Security 7, `ReactiveAuthorizationManager.authorize` returns `Mono<AuthorizationResult>` — in 6.x the method was named `check` and returned `Mono<AuthorizationDecision>`, which is the signature still shown in the commented fragment at the top of this page. `AuthorizationDecision` implements `AuthorizationResult`, but `Mono` is invariant: the cast inside the second `map` aligns the types. A plain `.map(AuthorizationDecision::new)` produces `Mono<AuthorizationDecision>` and no longer compiles as the method reference.
- The decision is purely name-based; roles play no part. Even `admin` is denied on `/users/user` because the names differ. To let admins bypass the check, extend the predicate with an authority check of your own.

Rules are evaluated in registration order and the first match wins, so the specific `/users/{user}/**` rule must come before the catch-all `anyExchange().authenticated()`. The sample also enables HTTP Basic (`httpBasic(withDefaults())`) so credentials can be sent with curl's `-u` option.

The sample's `UserController` exposes two endpoints:

| Endpoint | Rule | Result |
|----------|------|--------|
| `GET /user` | `anyExchange().authenticated()` | any authenticated user; returns the current principal |
| `GET /users/{username}` | `access(this::currentUserMatchesPath)` | only when `{username}` equals the authenticated name |

Verify with curl — the sample seeds `user`/`password` and `admin`/`password` into Mongo on startup:

```
# own path — granted
curl -v http://localhost:8080/users/user -u "user:password"
< HTTP/1.1 200 OK

# someone else's path — denied
curl -v http://localhost:8080/users/admin -u "user:password"
< HTTP/1.1 403 Forbidden
```

[SecurityDataMongoTests](https://github.com/hantsy/spring-reactive-sample/blob/master/security-data-mongo/src/test/java/com/example/demo/SecurityDataMongoTests.java) covers all of these cases — anonymous `401`, own path `200`, foreign path `403` — with `WebTestClient` against a Testcontainers-managed Mongo.

You can explore the complete codes in [spring-reactive-sample/security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo). The [security](https://github.com/hantsy/spring-reactive-sample/tree/master/security) sample shows the same rule commented out alongside the classic path-matching rules.
