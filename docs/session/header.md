
# Header-based session id

By default, WebFlux resolves the session id from the `SESSION` cookie. Browser clients are fine with this, but API clients — mobile apps, server-to-server calls — often prefer to carry the session id in a header.

`WebSessionIdResolver` is the extension point that decides how a session id is resolved from a request and written back to a response. Spring ships `CookieWebSessionIdResolver` (the default) and `HeaderWebSessionIdResolver`.

## Step 1: register a custom resolver

```java
@Configuration
@EnableSpringWebSession
public class SessionConfig {

    @Bean
    public ReactiveSessionRepository<?> sessionRepository() {
        return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
    }

    @Bean
    public WebSessionIdResolver headerWebSessionIdResolver() {
        HeaderWebSessionIdResolver resolver = new HeaderWebSessionIdResolver();
        resolver.setHeaderName("X-SESSION-ID");
        return resolver;
    }
}
```

`HeaderWebSessionIdResolver` reads the session id from the `X-SESSION-ID` request header and returns it in the same response header.

## Step 2: secure the application

The sample combines the header resolver with Spring Security, HTTP Basic, form login, and a session-based security context repository:

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
       return http
               .csrf(csrf -> csrf.disable())
               .authorizeExchange(exchange -> exchange
                       .anyExchange().authenticated())
               .httpBasic(basic -> basic.securityContextRepository(new WebSessionServerSecurityContextRepository()))
               .formLogin(withDefaults())
               .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsRepository() {
        var userBuilder = User.withDefaultPasswordEncoder();
        UserDetails user = userBuilder.username("user").password("password").roles("USER").build();
        UserDetails admin = userBuilder.username("admin").password("password").roles("USER", "ADMIN").build();
        return new MapReactiveUserDetailsService(user, admin);
    }
}
```

A controller exposes the current session id for verification:

```java
@RestController
public class SessionController {

    @GetMapping("/sessionId")
    public Map<String, String> sessionId(WebSession session){
        Map<String, String> map = new HashMap<>();
        map.put("id", session.getId());
        return map;
    }
}
```

## Verify

1. Authenticate and request the session id:

    ```bash
    curl -u user:password http://localhost:8080/sessionId
    # {"id":"..."}
    ```

    The response carries the `X-SESSION-ID` header with the same value.

2. Call a protected endpoint with the header instead of Basic auth:

    ```bash
    curl -H "X-SESSION-ID: <id>" http://localhost:8080/posts
    ```

The test in the sample asserts exactly this contract:

```java
FluxExchangeResult<Map<String, String>> result = this.rest
        .get()
        .uri("/sessionId")
        .header("Authorization", getBasicAuth())
        .exchange()
        .returnResult(new ParameterizedTypeReference<Map<String, String>>() {});

assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);

List<String> sessionHeaders = result.getResponseHeaders().get("X-SESSION-ID");
assertThat(sessionHeaders.size()).isEqualTo(1);

String sessionId = result.getResponseBody().blockFirst().get("id");
assertThat(sessionHeaders.get(0)).isEqualTo(sessionId);

this.rest
        .get()
        .uri("/posts")
        .header("X-SESSION-ID", sessionId)
        .exchange()
        .expectStatus().isOk();
```

For the complete codes, check [spring-reactive-sample/session-header](https://github.com/hantsy/spring-reactive-sample/tree/master/session-header).
