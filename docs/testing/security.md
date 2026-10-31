
# Testing Spring Security

Secured endpoints change what a web-layer test means: an unauthenticated request should get 401, a wrong-role request 403, and a valid one should pass. Spring Security's test support (`spring-security-test`, shipped as `spring-boot-starter-security-test` in Boot) provides the tools — mock users, JWT/OIDC authentication, CSRF handling, and ways to skip the security layer entirely.

The samples referenced here are [security](https://github.com/hantsy/spring-reactive-sample/tree/master/security), [security-method](https://github.com/hantsy/spring-reactive-sample/tree/master/security-method), [session-header](https://github.com/hantsy/spring-reactive-sample/tree/master/session-header), and [boot-exception-handler](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-exception-handler).

## Add the test dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

For a plain Spring (non-Boot) application, use `org.springframework.security:spring-security-test` instead.

## Wire security into WebTestClient

A `WebTestClient` bound to an application context does **not** apply the security filters by default. Activate them with the `springSecurity()` configurer — this installs the `WebFilterChainProxy` into the mock environment, exactly as at runtime:

```java
@BeforeEach
public void setup() {
    this.rest = WebTestClient
            .bindToApplicationContext(this.context)
            .apply(springSecurity())
            //.apply(csrf())
            .configureClient()
            .build();
}
```

From this point, the test client behaves like a real browser: the HTTP Basic challenge, form login redirect, and CSRF protection all apply, and the following sections authenticate against them.

## Mock users

### HTTP Basic credentials

The simplest approach against an HTTP Basic-protected API — send real credentials as headers, which exercises the whole authentication path including the `ReactiveUserDetailsService`:

```java
@Test
public void savingPostsWhenInvalidCredentialsThenUnauthorized() {
    this.rest
            .post()
            .uri("/posts")
            .body(BodyInserters.fromValue(Post.builder().title("title test").content("content test").build()))
            .headers(invalidCredentials())
            .exchange()
            .expectStatus().is4xxClientError();
}

private Consumer<HttpHeaders> invalidCredentials() {
    return httpHeaders -> httpHeaders.setBasicAuth("user", "INVALID");
}
```

The [session-header](https://github.com/hantsy/spring-reactive-sample/tree/master/session-header) sample uses the same pattern with the `Authorization` header built explicitly.

### mutateWith(mockUser())

To bypass authentication and inject a principal directly, use `SecurityMockServerConfigurers.mockUser()` — it authenticates the request as an in-memory user without touching the `ReactiveUserDetailsService`:

```java
@Test
public void deletingPostsWhenUserCredentialsThenForbidden_mutateWith() {
    this.rest
            .mutateWith(mockUser().password("password"))
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().is4xxClientError();
}
```

`mockUser()` defaults to username `"user"` with role `USER`; customize with `.username(...)`, `.roles(...)`, `.authorities(...)`, or pass a prepared `UserDetails`.

### @WithMockUser

The annotation-driven variant sets the authentication for a whole test class or a single method:

```java
@SpringBootTest(classes = {DemoApplication.class, TestcontainersConfiguration.class})
@WithMockUser
public class DemoApplicationTests {
    // every test in this class runs as the default mock user
}

@Test
@WithMockUser()
public void deletingPostsWhenUserCredentialsThenForbidden_withMockUserAnnotation() {
    this.rest
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().is4xxClientError();
}
```

`@WithMockUser(username = "admin", roles = "ADMIN")` elevates the principal; `@WithAnonymousUser` and `@WithSecurityContext` cover the remaining cases.

## Testing method-level security

`@PreAuthorize` rules are exercised by driving the request with a user of the wrong role and asserting 403 — the [security-method](https://github.com/hantsy/spring-reactive-sample/tree/master/security-method) sample protects `delete` with `hasRole('ADMIN')` and verifies a plain `USER` cannot reach it, both with `mutateWith` and with `@WithMockUser`:

```java
@Test
public void deletingPostsWhenUserCredentialsThenForbidden_mutateWith() {
    this.rest
            .mutateWith(mockUser().password("password"))   // role USER
            .delete()
            .uri("/posts/1")
            .exchange()
            .expectStatus().is4xxClientError();            // 403, not 401
}
```

The distinction matters: 401 means unauthenticated, 403 means authenticated but not authorized — `mockUser()` should always produce the latter on an authorization failure.

## Mocking JWTs and OIDC

For resource servers validating JWTs or OIDC tokens, the mutators fabricate a token without a real issuer:

```java
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@Test
public void readWithJwtScope() {
    this.rest
            .mutateWith(mockJwt().jwt(jwt -> jwt
                    .subject("test-subject")
                    .claim("scope", "read")))
            .get()
            .uri("/posts")
            .exchange()
            .expectStatus().isOk();
}
```

`mockJwt()` authenticates as a `JwtAuthenticationToken`; control the token with the `Jwt.Builder` consumer (`subject`, `claim`, `audience`, `expiresAt`, ...) or build a complete `Jwt` and pass it to `mockJwt(jwt)`. The OIDC counterpart is `mockOidcLogin()` — it authenticates as an `OidcUser` with a configurable ID token and user-info claims:

```java
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

this.rest
        .mutateWith(mockOidcLogin().idToken(token -> token.subject("alice")))
        .get()
        .uri("/profile")
        .exchange()
        .expectStatus().isOk();
```

Both mutators need the JOSE machinery on the test classpath — add `org.springframework.security:spring-security-oauth2-jose` with test scope when the application itself is not a resource server.

## CSRF

CSRF protection rejects mutating requests (POST/PUT/DELETE) that lack the token. Tests that authenticate real users should add the CSRF configurator — either once on the client builder or per request:

```java
// once for all requests
this.rest = WebTestClient.bindToApplicationContext(this.context)
        .apply(springSecurity())
        .apply(csrf())
        .configureClient()
        .build();

// or per request
this.rest.mutateWith(csrf()).post().uri("/posts")...
```

When tests authenticate with `mockUser()`/`mockJwt()` the CSRF filter is already satisfied — you need the explicit token mainly for tests that exercise real authentication (Basic, form login).

## Skipping security

Some web-layer tests should not deal with security at all. Two verified ways to skip it:

### Exclude the security auto-configuration

In a Boot test, remove the security configuration from the slice — the Boot 4 reactive auto-configuration classes live in `org.springframework.boot.security.autoconfigure`:

```java
@WebFluxTest(controllers = PostController.class, excludeAutoConfiguration = {
        org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration.class,
        org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
        org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration.class
})
class PostControllerWithoutSecurityTest {
    // no security filters in this context — every request passes through
}
```

The property form works for `@SpringBootTest` too:

```properties
spring.autoconfigure.exclude=\
  org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration,\
  org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
```

### Override the filter chain with a permissive one

Alternatively, keep the auto-configuration and supply a `SecurityWebFilterChain` bean that permits everything — a user-declared chain always replaces the Boot default:

```java
@TestConfiguration
static class PermitAllSecurityConfig {

    @Bean
    SecurityWebFilterChain permitAll(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
    }
}

@WebFluxTest(controllers = PostController.class)
@Import(PermitAllSecurityConfig.class)
class PostControllerOpenTest {
}
```

!!! note "Prefer mock users for security tests"

    Excluding security verifies that your controllers work — it does not verify your security rules. Use the exclusion for tests focused on other concerns, and the `mockUser()`/`mockJwt()` mutators from the sections above when the rules themselves are under test.
