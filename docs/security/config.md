
# Configure Spring Security

When you are developing a Spring WebFlux application without Spring Boot, add the security dependencies from the [previous page](start.md) to your project.

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

The commented `currentUserMatchesPath` fragment above demonstrates a dynamic rule: it grants access only when the authenticated username matches the `{user}` path variable of the exchange. Wire it with `.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)` to activate it.

For the complete codes, check [spring-reactive-sample/security](https://github.com/hantsy/spring-reactive-sample/tree/master/security) and [spring-reactive-sample/security-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo), which applies the same rule against a Mongo-backed user store.
