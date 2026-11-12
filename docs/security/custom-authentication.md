# Custom authentication

Built-in mechanisms cover the common credential formats. When your clients send something else — a header of your own design, a device id, a signature — Spring Security has no customizer to enable, and you do not need one. The mechanism is assembled from the same four parts `httpBasic` and `formLogin` use internally, plus one registration call.

This page builds that mechanism around the [security-api-key](https://github.com/hantsy/spring-reactive-sample/tree/master/security-api-key) sample: client applications authenticate with an opaque key in an `X-API-KEY` header, and nothing else changes about how the chain runs. Note that, unlike [form login](built-in-authentication.md), these credentials ride every request — there is no login page, no session, and no cookie in the design.

## The shape of a mechanism

Every authentication mechanism is the same pipeline, so adding one means supplying the pieces that are specific to your credential format:

1. An `Authentication` token type — the object that carries the raw credential before validation and the resolved principal after it.
2. A `ServerAuthenticationConverter` — reads the credential off the `ServerWebExchange` and returns the unauthenticated token.
3. A `ReactiveAuthenticationManager` — validates the token and returns the authenticated one, or signals failure.
4. An `AuthenticationWebFilter` — wires 1-3 together, and decides when they apply at all through a `ServerWebExchangeMatcher`.
5. One registration call on `ServerHttpSecurity` — `addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)`, or `addFilterBefore`/`addFilterAfter` to place it relative to an existing filter.

You write no `WebFilter` of your own from scratch, no code to save the `SecurityContext`, and no code to write the rejection response. `AuthenticationWebFilter` already does that work: it runs the matcher, calls the converter, subscribes to the manager, stores the result through the chain's `ServerSecurityContextRepository`, and on `AuthenticationException` hands off to its failure handler. Overriding those four settings is the entire mechanism.

## Worked example: an `X-API-KEY` header

### Step 1: the token

A key belongs to a client application, not to a user account, so the sample gives the credential its own principal type. `ClientApp` pairs a name with the opaque secret that stands for it:

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

### Step 2: the converter

The converter reads the header and hands the raw value to the token factory:

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

An empty `Mono` from `convert(...)` tells `AuthenticationWebFilter` that this request carries no credentials for this mechanism, so it moves on unchanged. `API_KEY_HEADER` is public because the header name is part of the contract with callers — it is what the README, the tests, and the curl transcript below all quote.

### Step 3: the manager

The manager asks the store who owns the key and grants a single fixed role to every client application:

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

### Step 4: the filter

The filter collects the collaborators and adds the wiring that makes the mechanism fire only for requests carrying the header:

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

### Step 5: register it

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
6. `csrf` is disabled in both chains: neither mechanism relies on a cookie a browser could be tricked into sending, which is the attack `CsrfWebFilter` exists to stop. That exemption is a property of these transports, not a habit to carry into a cookie-based mechanism — see the CSRF discussion in [Built-in authentication mechanisms](built-in-authentication.md).

## Registering client apps

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

## Which mechanism answers a request

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

The [security-api-key](https://github.com/hantsy/spring-reactive-sample/tree/master/security-api-key) sample exposes `GET /user`, which renders the current `Authentication` as name, authorities, and the authenticated flag; `GET /api/posts`, the general-purpose API for client applications; and `GET`/`POST /clients`, the admin-only registry behind them. The same pipeline is what the built-in mechanisms are built from — compare it against [Built-in authentication mechanisms](built-in-authentication.md) — and once authentication is settled, [Method level constraints](method.md) moves authorization next to the business methods themselves.
