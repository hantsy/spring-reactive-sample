# HTTP Basic and API key authentication

Authorization rules decide whether a request may pass through the filter chain; authentication mechanisms establish who is making it. Every mechanism in a WebFlux application is a `WebFilter` inside the `SecurityWebFilterChain`: it converts the request's credentials into an `Authentication` token, passes the token to a `ReactiveAuthenticationManager`, and on success puts the authenticated result into the `SecurityContext` for the rest of the chain.

This page covers two request-level mechanisms: the built-in HTTP Basic support, for human users, and a custom API key mechanism for client applications that send an `X-API-KEY` header. Unlike [Form based authentication](form.md), the client sends credentials with every request; no login page or session is involved.

The two never meet. The [security-api-key](https://github.com/hantsy/spring-reactive-sample/tree/master/security-api-key) sample puts each mechanism in its own `SecurityWebFilterChain` and splits the application by path: everything under `/api/**` is authenticated by API keys and nothing else, everything outside it by HTTP Basic.

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

Machine-to-machine clients often authenticate with an API key in a custom header instead of username/password credentials. Spring Security ships no built-in API key mechanism, and you do not need one: the sample reuses the same `AuthenticationWebFilter` machinery and supplies the pieces specific to API keys — a store of the client applications that own the keys, a token type, a manager, a converter, and a filter that wires them together.

### The client app store

A key belongs to a client application, not to a user account, so the sample gives it its own entity. `ClientApp` pairs a name with the opaque secret that stands for it:

```java
record ClientApp(String name, String apiKey) {

    static ClientApp registered(String name) {
        return new ClientApp(name, UUID.randomUUID().toString());
    }

}
```

`ClientAppRepository` keeps the applications in a `ConcurrentHashMap` keyed by the API key, which is exactly the query authentication needs:

```java
@Component
class ClientAppRepository {

    private final Map<String, ClientApp> byApiKey = new ConcurrentHashMap<>();

    ClientAppRepository() {
        save(new ClientApp("web-client", "web-client-api-key"));
        save(new ClientApp("mobile-client", "mobile-client-api-key"));
    }

    Mono<ClientApp> findByApiKey(String apiKey) {
        return Mono.justOrEmpty(this.byApiKey.get(apiKey));
    }

    Flux<ClientApp> findAll() {
        return Flux.fromIterable(this.byApiKey.values());
    }

    Mono<ClientApp> save(ClientApp clientApp) {
        this.byApiKey.put(clientApp.apiKey(), clientApp);
        return Mono.just(clientApp);
    }

}
```

1. `findByApiKey` is the authentication lookup. An unknown key yields an empty `Mono` rather than `null`, so the manager decides what absence means.
2. The same store serves administration: `findAll` backs `GET /clients` and `save` backs `POST /clients`. A key registered through the admin endpoint is live for authentication immediately — one source of truth for both jobs.
3. Two applications are seeded with fixed keys so the sample is reproducible; `registered(...)` shows how a generated key differs from a hand-assigned one. Swap the map for a real store — and hashed keys — in production.

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
2. The manager returns the `authenticated` form: the client app name the key maps to, its granted authorities, and the authenticated flag set to true.
3. `eraseCredentials()` clears the key so it is not held after the exchange completes. `getName()` comes from `AbstractAuthenticationToken` and resolves to the principal, so downstream code reads the authenticated application name with `authentication.getName()`.

### The authentication manager

`ApiKeyAuthenticationManager` asks the store who owns the key and grants a single fixed role to every client application:

```java
class ApiKeyAuthenticationManager implements ReactiveAuthenticationManager {

    private static final String ROLE_APP = "ROLE_APP";

    private final ClientAppRepository clientApps;

    ApiKeyAuthenticationManager(ClientAppRepository clientApps) {
        this.clientApps = clientApps;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String apiKey = (String) authentication.getCredentials();
        return this.clientApps.findByApiKey(apiKey)
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid API key")))
            .map(clientApp -> ApiKeyAuthentication.authenticated(clientApp.name(), apiKey,
                AuthorityUtils.createAuthorityList(ROLE_APP)));
    }

}
```

1. An empty lookup becomes a `BadCredentialsException` through `switchIfEmpty`. `AuthenticationWebFilter` catches `AuthenticationException` and routes it to its failure handler, so a wrong key never reaches the controller.
2. The principal is the application name, the authority is `ROLE_APP` for all of them. Per-application roles would belong to the store, not to the manager; until then, `hasRole("APP")` is what the `/api` chain asks for and what every registered application satisfies.

### The authentication converter

`ApiKeyAuthenticationConverter` reads the header and hands the raw value to the token factory:

```java
class ApiKeyAuthenticationConverter implements ServerAuthenticationConverter {

    public static final String API_KEY_HEADER = "X-API-KEY";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER))
            .map(ApiKeyAuthentication::unauthenticated);
    }

}
```

An empty `Mono` from `convert(...)` tells `AuthenticationWebFilter` that this request carries no credentials for this mechanism, so it moves on unchanged. `API_KEY_HEADER` is public because the header name is part of the contract with callers — it is what the README, the tests, and the curl transcripts below all quote.

### The authentication filter

`ApiKeyAuthenticationFilter` collects the collaborators and adds the wiring that makes the mechanism fire only for requests carrying the header:

```java
class ApiKeyAuthenticationFilter extends AuthenticationWebFilter {

    ApiKeyAuthenticationFilter(ClientAppRepository clientApps) {
        super(new ApiKeyAuthenticationManager(clientApps));
        setServerAuthenticationConverter(new ApiKeyAuthenticationConverter());
        setRequiresAuthenticationMatcher(this::apiKeyPresent);
        setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(
            new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));
    }

    private String extractApiKeyFromHttpHeaders(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(ApiKeyAuthenticationConverter.API_KEY_HEADER);
    }

    private Mono<ServerWebExchangeMatcher.MatchResult> apiKeyPresent(ServerWebExchange exchange) {
        return StringUtils.hasText(extractApiKeyFromHttpHeaders(exchange))
            ? ServerWebExchangeMatcher.MatchResult.match()
            : ServerWebExchangeMatcher.MatchResult.notMatch();
    }

}
```

`ServerWebExchangeMatcher` is a functional interface returning a `Mono<MatchResult>`, so the gate can be a method reference on the filter itself. Three settings matter:

1. `setRequiresAuthenticationMatcher(...)` decides whether the filter applies at all, before any conversion happens. A request without the header is left completely alone — no token, no manager call, no failure.
2. `setAuthenticationFailureHandler(...)` replaces the default. Left alone, the failure handler calls `HttpBasicServerAuthenticationEntryPoint`, which would answer a rejected API key with a `WWW-Authenticate: Basic` challenge — inviting a machine client to retry with credentials it was never meant to have. `HttpStatusServerEntryPoint` writes a bare `401` instead.
3. `setServerAuthenticationConverter(...)` swaps the Basic converter for the header reader above. The repository is passed through to the manager, and the `NoOpServerSecurityContextRepository` default stays: an API key is resent with every request, so there is nothing worth persisting between them.

### Two ordered chains

The filter is registered in a chain of its own, matching only the machine-facing paths:

```java
@Configuration
class ApiKeySecurityConfig {

    @Bean
    @Order(1)
    SecurityWebFilterChain apiKeySecurityWebFilterChain(ServerHttpSecurity http, ClientAppRepository clientApps) {
        return http
            .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"))
            .csrf(csrf -> csrf.disable())
            .addFilterAt(new ApiKeyAuthenticationFilter(clientApps), SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange(exchange -> exchange
                .anyExchange().hasRole("APP"))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
            .build();
    }

}
```

and the conventional Basic chain takes the rest:

```java
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    @Order(2)
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .httpBasic(withDefaults())
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/clients").hasRole("ADMIN")
                .anyExchange().authenticated())
            .build();
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user").password("password").roles("USER").build();
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin").password("password").roles("USER", "ADMIN").build();
        return new MapReactiveUserDetailsService(user, admin);
    }

}
```

1. `@EnableWebFluxSecurity` belongs to a single configuration class; the other one only contributes its chain bean. `ServerHttpSecurity` is a prototype-scoped bean, so each chain method receives its own instance to configure — that is what makes several chains possible at all.
2. `securityMatcher(...)` restricts a chain to the requests it owns. It takes a `ServerWebExchangeMatcher`, not a `String`, hence `ServerWebExchangeMatchers.pathMatchers("/api/**")`. The first chain whose matcher matches handles the request and the remaining ones are skipped entirely — exclusivity comes from the matcher, not from inspecting headers inside a filter.
3. `@Order` is load-bearing. Without it the chains run in bean-definition order, which is not something to rely on; giving the Basic chain the earlier value makes it match every path first and the `/api` tests fail — a bad key answers with a Basic challenge again and the `ROLE_APP` rule is never reached.
4. Because a path is served by exactly one chain, each chain carries one mechanism, and neither mechanism has to know the other exists: `Authorization` is invisible to `/api/**` and `X-API-KEY` is invisible everywhere else. The `exceptionHandling` entry point on the API chain keeps its rejections free of the `WWW-Authenticate: Basic` header that `httpBasic` would otherwise advertise.
5. Construct the filter with `new` here rather than declaring it as a bean. `WebHttpHandlerBuilder` collects all `WebFilter` beans, so a bean would run once standalone and again inside the security chain.
6. `csrf` is disabled in both chains: neither mechanism relies on a cookie a browser could be tricked into sending, which is the attack `CsrfWebFilter` exists to stop.

### Registering client apps

Management of the store deliberately lives outside `/api/**`, so it is guarded by HTTP Basic and an admin role rather than by the keys it hands out:

```java
@RestController
@RequestMapping("/clients")
class ClientAppController {

    record Registration(String name) {
    }

    private final ClientAppRepository clientApps;

    ClientAppController(ClientAppRepository clientApps) {
        this.clientApps = clientApps;
    }

    @GetMapping
    Flux<ClientApp> list() {
        return this.clientApps.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<ClientApp> register(@RequestBody Mono<Registration> registration) {
        return registration
            .map(entry -> ClientApp.registered(entry.name()))
            .flatMap(this.clientApps::save);
    }

}
```

`POST /clients` takes only a name, generates the key, and returns both. The new key authenticates against the same repository the manager reads, so the application can call `/api/**` immediately — `adminRegistersClientAppAndNewKeyAccessesApiPath` asserts exactly that round trip.

### Which mechanism answers a request

| Path | Credentials sent | Chain that owns it | Result |
| --- | --- | --- | --- |
| `/user` | none | Basic | `401` with a `WWW-Authenticate: Basic` challenge |
| `/user` | valid `Authorization` | Basic | `200`, authenticated as that user |
| `/user` | valid `X-API-KEY` | Basic | `401` — outside `/api/**` a key is not a credential |
| `/api/posts` | valid `X-API-KEY` | API key | `200`, authenticated as the client app |
| `/api/posts` | none | API key | `401` with no challenge header |
| `/api/posts` | invalid `X-API-KEY` | API key | `401` — the chain stops at the filter |
| `/api/posts` | valid `Authorization` | API key | `401` — this chain has no Basic mechanism |
| `/api/posts` | valid `X-API-KEY` and `Authorization` | API key | `200`, authenticated as the client app |
| `/clients` | `admin` Basic | Basic | `200` / `201` |
| `/clients` | `user` Basic | Basic | `403` — authenticated, but not `ROLE_ADMIN` |

The last row is the distinction worth remembering: credentials that are not accepted at all produce `401` and run the chain's entry point, while an accepted identity that fails a role check produces `403`.

[SecurityApiKeyTests](https://github.com/hantsy/spring-reactive-sample/blob/master/security-api-key/src/test/java/com/example/demo/SecurityApiKeyTests.java) pins each row down, in `validApiKeyIsIgnoredOutsideApiPath`, `basicCredentialsAreNotCredentialsForTheApiPath`, `validApiKeyDecidesApiPathWhenBasicCredentialsAreAlsoSent`, `invalidApiKeyIsUnauthorized`, `invalidApiKeyChallengeDoesNotAdvertiseBasic`, and `nonAdminCannotListClientApps`.

## Verify with curl

Start the sample and try each mechanism:

```bash
# no credentials — 401 with a Basic challenge
curl -v http://localhost:8080/user
< HTTP/1.1 401 Unauthorized
< WWW-Authenticate: Basic realm="Realm"

# HTTP Basic — a human user
curl -s http://localhost:8080/user -u "user:password"
{"name":"user","authorities":["ROLE_USER"],"authenticated":true}

# API key on /api/** — authenticated as the client app that owns the key
curl -s http://localhost:8080/api/posts -H "X-API-KEY: web-client-api-key"
[{"title":"post one","content":"content of post one"},{"title":"post two","content":"content of post two"}]

# unknown key — 401, and no Basic challenge to mislead the caller
curl -v http://localhost:8080/api/posts -H "X-API-KEY: no-such-key"
< HTTP/1.1 401 Unauthorized

# Basic credentials on /api/** — this chain has no Basic mechanism
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/posts -u "admin:password"
401

# an API key outside /api/** — this chain has no key mechanism
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/user -H "X-API-KEY: web-client-api-key"
401

# administering the store needs an admin user, not a key
curl -s http://localhost:8080/clients -u "admin:password"
[{"name":"mobile-client","apiKey":"mobile-client-api-key"},{"name":"web-client","apiKey":"web-client-api-key"}]
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/clients -u "user:password"
403

# register a new application and use the key it is given
curl -s -X POST http://localhost:8080/clients -u "admin:password" \
  -H "Content-Type: application/json" -d '{"name":"batch-worker"}'
{"name":"batch-worker","apiKey":"b42db08b-f58c-485a-8b64-fe7d096aebcf"}
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/posts -H "X-API-KEY: b42db08b-f58c-485a-8b64-fe7d096aebcf"
200
```

## See it in code

The [security-api-key](https://github.com/hantsy/spring-reactive-sample/tree/master/security-api-key) sample exposes `GET /user`, which renders the current `Authentication` as name, authorities, and the authenticated flag; `GET /api/posts`, the general-purpose API for client applications; and `GET`/`POST /clients`, the admin-only registry behind them. Continue with [Method level constraints](method.md) to move authorization next to the business methods themselves.
