# Built-in authentication mechanisms

Authorization rules decide whether a request may pass through the filter chain; authentication mechanisms establish who is making it. Every mechanism in a WebFlux application is a `WebFilter` inside the `SecurityWebFilterChain`: it converts the request's credentials into an `Authentication` token, passes the token to a `ReactiveAuthenticationManager`, and on success puts the authenticated result into the `SecurityContext` for the rest of the chain.

This page covers the two mechanisms you reach for first — HTTP Basic for API-style clients and form login for browser pages — and then surveys the rest of the built-in set. All of them follow the same converter → manager → context shape, which is also the shape you implement when you add a mechanism of your own; see [Custom authentication](custom-authentication.md).

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
2. A `UserDetailsRepositoryReactiveAuthenticationManager` that validates the token against the application's `ReactiveUserDetailsService` bean — the in-memory `MapReactiveUserDetailsService` used across this chapter's samples, or a custom store as described in [Customize UserDetailsRepository](user-details-repo.md).
3. A `NoOpServerSecurityContextRepository`: the authenticated result is not persisted, so every request re-reads the header.
4. An entry point that answers rejected requests with `401` and a `WWW-Authenticate: Basic` challenge — the response seen in [Configure Spring Security](config.md).

Nothing about the mechanism depends on the credentials being a human's password. What makes Basic convenient for demos is that a single `curl -u` drives it, which is why the samples in this chapter use it; [Custom authentication](custom-authentication.md) covers the machine-to-machine case where a header of your own design fits better.

## Form login

Form login is the mechanism for browser-driven pages: instead of expecting credentials on every request, it redirects unauthenticated browsers to a login page, remembers the result in the server-side session, and lets subsequent requests ride the session cookie.

The [security-form](https://github.com/hantsy/spring-reactive-sample/tree/master/security-form) sample needs only one line beyond the Basic example above:

```java
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http.authorizeExchange(exchange -> exchange
                .anyExchange().authenticated())
                .formLogin(withDefaults())
                .build();
    }

    // ...
}
```

With that in place, a controller such as `HomeController` returning a view name (`"home"`) and a template that renders `Flux<Post>` into HTML is enough — the security side is entirely configuration, and the template never needs to know about it.

`formLogin(withDefaults())` installs four collaborators:

1. `LoginPageGeneratingWebFilter` serves a generated `/login` page, so an application gets a login form without writing one.
2. `AuthenticationWebFilter` at the `FORM_LOGIN` position, with a converter that reads the submitted `username` and `password` form fields and a success handler that redirects back into the application.
3. `RedirectServerAuthenticationEntryPoint`, which answers an unauthenticated request with `302` to the login page rather than a `401` challenge.
4. `WebSessionServerSecurityContextRepository`, which stores the authenticated `SecurityContext` in the `WebSession` — this is what makes the session cookie meaningful, and what distinguishes form login from Basic's `NoOp` repository.

Unlike the servlet stack, the WebFlux entry point does not inspect the request's `Accept` header. A single `formLogin` chain redirects every unauthenticated request, so an `XMLHttpRequest` or a `fetch` call expecting JSON also receives `302 → /login`. For an API, use Basic or a token mechanism instead.

Because the flow involves a browser round trip with a form POST, CSRF protection is on by default: the generated login page embeds a hidden `_csrf` input whose token is stored in the `WebSession` alongside the session cookie. A login POST without a valid `_csrf` parameter is rejected with `403` before any credential is checked.

To replace the generated page with your own, point the entry point somewhere else:

```java
.authorizeExchange(exchange -> exchange
        .pathMatchers("/signin").permitAll()
        .anyExchange().authenticated())
.formLogin(formLogin -> formLogin
        .loginPage("/signin"))
```

`loginPage(String)` does two things at once: unauthenticated requests now redirect to that URL, and `LoginPageGeneratingWebFilter` is no longer installed — Spring Security assumes your application serves the page. That makes the path part of your responsibility twice over: it needs a handler that renders the form, and it needs to be reachable without credentials. Leaving it behind `.anyExchange().authenticated()` redirects `GET /signin` to `/signin`, and the browser loops. The POST that the form submits is still handled by the security filter: `loginPage` derives the processing matcher from the same URL, so the form posts back to `/signin`.

## Other built-in mechanisms

The same DSL exposes the rest of the built-in set. Each is a method on `ServerHttpSecurity`, and each takes a customizer in the same shape as the two above:

| Method | Mechanism |
| :--- | :--- |
| `x509(...)` | Authenticates from the client certificate presented at the TLS layer, resolving the principal with an `X509PrincipalExtractor` |
| `oauth2Login(...)` | Full "Login with Google/GitHub/…" flow: authorization-code redirect, callback handling, then the same `SecurityContext` write |
| `oauth2Client(...)` | The client half on its own, for applications that call third-party APIs on a user's behalf without using OAuth2 to sign in |
| `oauth2ResourceServer(...)` | Validates an opaque or JWT bearer token issued by an authorization server — the usual fit for SPAs and service APIs |
| `oneTimeTokenLogin(...)` | Email-a-code sign-in, with a generated submit page like form login's |

The reactive DSL is not a mirror of the servlet one. There is no `rememberMe(...)` and no `oidcLogin(...)` customizer on `ServerHttpSecurity`, so an application that needs those has to reach for the servlet stack or build the behaviour itself. Alongside authentication, `redirectToHttps(...)`, `anonymous(...)`, and `logout(...)` round out request handling.

For configuration details of any mechanism beyond the two worked through here, see the WebFlux section of the [Spring Security reactive reference](https://docs.spring.io/spring-security/reference/reactive/index.html).

## Verify with curl

The redirect-then-POST-then-cookie flow is easy to confirm by hand. Start the [security-form](https://github.com/hantsy/spring-reactive-sample/tree/master/security-form) sample and follow the exchange.

An unauthenticated request is redirected, whatever it asks for in `Accept`:

```bash
curl -v http://localhost:8080/ -H "Accept: application/json"
< HTTP/1.1 302 Found
< Location: /login
```

Fetching the generated page starts a session and hands over the CSRF token:

```bash
curl -s -c cookies.txt http://localhost:8080/login
```

The relevant lines of the page it returns:

```html
<form class="login-form" method="post" action="/login">
    <input type="text" id="username" name="username" placeholder="Username" required autofocus>
    <input type="password" id="password" name="password" placeholder="Password" required>
    <input name="_csrf" type="hidden" value="OodBUCNtzabGjmrbPZm52oMvTmNuliuoeHZp2UJGvBL1nv4RDbEiZxRa_pPr61zjDrSN7eccYwJX90mFSEdZ7SRx2nfAq8Zw" />
```

and the response sets `SESSION=...; Path=/; HTTPOnly; SameSite=Lax`. Pull the token out of that page and POST the form with the cookie it came from:

```bash
CSRF=$(curl -s -c cookies.txt http://localhost:8080/login \
  | grep -o 'name="_csrf" type="hidden" value="[^"]*' | sed 's/.*value="//')

curl -v -b cookies.txt -c cookies.txt -X POST http://localhost:8080/login \
  -d "username=user" -d "password=password" -d "_csrf=$CSRF"
< HTTP/1.1 302 Found
< Location: /
< set-cookie: SESSION=e288933b-aae9-4cb6-86bb-c97f6948cfbc; Path=/; HTTPOnly; SameSite=Lax
```

The redirect to `/` is the success handler; note the session id changed, which is session-fixation protection rotating it at authentication. Now the cookie alone carries the identity:

```bash
curl -s -b cookies.txt http://localhost:8080/
```

renders the home page with the seeded posts. Two failures round out the picture: a wrong password redirects to `/login?error`, and dropping the `_csrf` parameter from the POST yields `403` without consulting the user store.

## See it in code

[security-form](https://github.com/hantsy/spring-reactive-sample/tree/master/security-form) is the form login sample; HTTP Basic appears in [security-method](https://github.com/hantsy/spring-reactive-sample/tree/master/security-method) and [security-user-properties](https://github.com/hantsy/spring-reactive-sample/tree/master/security-user-properties). When none of the built-in mechanisms fits the credentials your clients actually send, build one in [Custom authentication](custom-authentication.md).
