# HTTP Basic and API key authentication

Authorization rules decide whether a request may pass through the filter chain; authentication mechanisms establish who is making it. Every mechanism in a WebFlux application is a `WebFilter` inside the `SecurityWebFilterChain`: it converts the request's credentials into an `Authentication` token, passes the token to a `ReactiveAuthenticationManager`, and on success puts the authenticated result into the `SecurityContext` for the rest of the chain.

This page covers two request-level mechanisms: the built-in HTTP Basic support, and a custom API key mechanism that reads an `X-API-KEY` header. Unlike [Form based authentication](form.md), the client sends credentials with every request; no login page or session is involved.

## HTTP Basic

Enable HTTP Basic with `httpBasic(withDefaults())`:

```java
@Bean
SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
    return http
        .httpBasic(withDefaults())
        .authorizeExchange(exchange -> exchange
            .anyExchange().authenticated())
        .build();
}
```

The customizer installs an `AuthenticationWebFilter` at the `HTTP_BASIC` position in the chain, wired with:

1. A `ServerHttpBasicAuthenticationConverter` that decodes the `Authorization: Basic ...` header into a username/password token.
2. A `UserDetailsRepositoryReactiveAuthenticationManager` that validates the token against the application's `ReactiveUserDetailsService` bean — the in-memory `MapReactiveUserDetailsService` in this page's sample, or a custom store as described in [Customize UserDetailsRepository](user-details-repo.md).
3. A `NoOpServerSecurityContextRepository`: the authenticated result is not persisted, so every request re-reads the header.
4. An entry point that answers rejected requests with `401` and a `WWW-Authenticate: Basic` challenge — the response seen in [Configure Spring Security](config.md).

## Custom API key authentication

Machine-to-machine clients often authenticate with an API key in a custom header instead of username/password credentials. Spring Security ships no built-in API key mechanism, and you do not need one: the [security-api-key](https://github.com/hantsy/spring-reactive-sample/tree/master/security-api-key) sample reuses the same `AuthenticationWebFilter` machinery and supplies only the pieces specific to API keys — a token type, a manager, and a converter.

### The authentication token

`ApiKeyAuthentication` carries the raw key before validation and the resolved principal after it:

```java
public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private final String principal;
    private String apiKey;

    private ApiKeyAuthentication(String principal, String apiKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.apiKey = apiKey;
    }

    public static ApiKeyAuthentication unauthenticated(String apiKey) {
        return new ApiKeyAuthentication(null, apiKey, List.of());
    }

    public static ApiKeyAuthentication authenticated(String principal, String apiKey, Collection<? extends GrantedAuthority> authorities) {
        ApiKeyAuthentication authentication = new ApiKeyAuthentication(principal, apiKey, authorities);
        authentication.setAuthenticated(true);
        return authentication;
    }

    @Override
    public Object getCredentials() {
        return this.apiKey;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.apiKey = null;
    }
}
```

1. The converter creates the `unauthenticated` form: the raw key, no principal, and `isAuthenticated()` false — the state every token must be in before a manager has vouched for it.
2. The manager returns the `authenticated` form: the username the key maps to, its granted authorities, and the authenticated flag set to true.
3. `eraseCredentials()` clears the key so it is not held after the exchange completes. `getName()` comes from `AbstractAuthenticationToken` and resolves to the principal, so downstream code reads the authenticated username with `authentication.getName()`.

### The authentication manager

`ApiKeyAuthenticationManager` looks the key up in an in-memory map; replace the map with a real store in production:

```java
class ApiKeyAuthenticationManager implements ReactiveAuthenticationManager {

    private final Map<String, String> apiKeys;

    ApiKeyAuthenticationManager(Map<String, String> apiKeys) {
        this.apiKeys = apiKeys;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String apiKey = (String) authentication.getCredentials();
        String username = this.apiKeys.get(apiKey);
        if (username == null) {
            return Mono.error(new BadCredentialsException("Invalid API key"));
        }
        List<GrantedAuthority> authorities = "admin".equals(username)
            ? AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")
            : AuthorityUtils.createAuthorityList("ROLE_USER");
        return Mono.just(ApiKeyAuthentication.authenticated(username, apiKey, authorities));
    }
}
```

An unknown key produces a `BadCredentialsException`. `AuthenticationWebFilter` catches `AuthenticationException` and routes it to its failure handler, whose default entry point answers `401` — a wrong key never reaches the controller.

### Registering the filter before HTTP Basic

The chain registers both mechanisms:

```java
@Bean
SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(csrf -> csrf.disable())
        .httpBasic(withDefaults())
        .addFilterBefore(apiKeyAuthenticationFilter(), SecurityWebFiltersOrder.HTTP_BASIC)
        .authorizeExchange(exchange -> exchange
            .anyExchange().authenticated())
        .build();
}

// Not a bean: WebHttpHandlerBuilder would also pick it up as a top-level WebFilter.
private AuthenticationWebFilter apiKeyAuthenticationFilter() {
    AuthenticationWebFilter filter = new AuthenticationWebFilter(
        new ApiKeyAuthenticationManager(Map.of("user-api-key", "user", "admin-api-key", "admin")));
    filter.setServerAuthenticationConverter(exchange
        -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-API-KEY"))
            .map(ApiKeyAuthentication::unauthenticated));
    return filter;
}
```

1. The converter reads the `X-API-KEY` header. When the header is absent, the converter returns an empty `Mono`, which signals `AuthenticationWebFilter` to skip authentication and hand the request to the next filter — Basic gets its turn.
2. `addFilterBefore(..., SecurityWebFiltersOrder.HTTP_BASIC)` places the API key filter ahead of the Basic filter, so a request carrying a valid key authenticates without `Authorization` credentials.
3. The filter needs no repository configuration: the default `NoOpServerSecurityContextRepository` matches the stateless nature of an API key, which the client sends on every request.
4. Keep the filter out of the application context as a bean. `WebHttpHandlerBuilder` collects all `WebFilter` beans, so a bean here would run once standalone and again inside the security chain.

Each successful authentication replaces the `SecurityContext` for the rest of the chain, and mechanisms run in chain order. A request carrying BOTH a valid `X-API-KEY` and valid Basic credentials therefore authenticates twice, and the later mechanism — Basic — decides the identity the controller sees. [SecurityApiKeyTests](https://github.com/hantsy/spring-reactive-sample/blob/master/security-api-key/src/test/java/com/example/demo/SecurityApiKeyTests.java) pins this behavior down in `basicReplacesApiKeyContextWhenBothHeadersPresent`.

## Verify with curl

Start the sample and try each mechanism:

```bash
# no credentials — 401 with a Basic challenge
curl -v http://localhost:8080/user
< HTTP/1.1 401 Unauthorized
< WWW-Authenticate: Basic realm="Realm"

# valid API key — authenticated as the key's owner, no session involved
curl -s http://localhost:8080/user -H "X-API-KEY: user-api-key"
{"name":"user","authorities":["ROLE_USER"],"authenticated":true}

# unknown key — rejected before the controller runs
curl -v http://localhost:8080/user -H "X-API-KEY: wrong-key"
< HTTP/1.1 401 Unauthorized

# HTTP Basic still works as the fallback
curl -s http://localhost:8080/user -u "admin:password"
{"name":"admin","authorities":["ROLE_ADMIN","ROLE_USER"],"authenticated":true}

# both headers — the later mechanism (Basic) decides the identity
curl -s http://localhost:8080/user -H "X-API-KEY: admin-api-key" -u "user:password"
{"name":"user","authorities":["ROLE_USER"],"authenticated":true}
```

## See it in code

The [security-api-key](https://github.com/hantsy/spring-reactive-sample/tree/master/security-api-key) sample exposes a single endpoint, `GET /user`, which renders the current `Authentication`: name, authorities, and the authenticated flag. Continue with [Method level constraints](method.md) to move authorization next to the business methods themselves.