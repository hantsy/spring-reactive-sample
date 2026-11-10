# CORS

Browsers enforce the same-origin policy: a page served from one origin may not freely call an API on another origin. Cross-Origin Resource Sharing (CORS) relaxes this through response headers such as `Access-Control-Allow-Origin`. For requests that are not "simple", the browser first sends a preflight `OPTIONS` request carrying `Origin` and `Access-Control-Request-Method`, and only proceeds when the server answers with matching `Access-Control-Allow-*` headers.

WebFlux offers three levels of CORS configuration:

- a `CorsWebFilter` bean — global rules applied in the `WebFilter` chain, before handler mapping
- `WebFluxConfigurer#addCorsMappings` — global rules applied at the handler-mapping level
- `@CrossOrigin` — fine-grained rules on a controller class or handler method

The examples referenced here live in this repository: [cors](https://github.com/hantsy/spring-reactive-sample/tree/master/cors) (manual configuration) and [boot-cors](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-cors) (Spring Boot customization).

## Manual configuration with CorsWebFilter

`CorsWebFilter` is a `WebFilter` that evaluates a `CorsConfigurationSource` for every request. Pair it with `UrlBasedCorsConfigurationSource` to map path patterns to CORS rules:

```java
@Configuration
@EnableWebFlux
public class WebConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://allowed-origin.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
```

Both `WebHttpHandlerBuilder` and Spring Boot pick up `WebFilter` beans automatically, so no further registration is needed. Requests from any other origin are rejected with `403 FORBIDDEN`; requests without an `Origin` header pass through untouched.

## Alternative: WebFluxConfigurer

If the application already implements `WebFluxConfigurer`, CORS mappings can be declared through `addCorsMappings` instead of a filter bean:

```java
public class CorsWebFluxConfigurer implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://allowed-origin.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

These mappings are applied by the handler mappings rather than the filter chain. In the [cors](https://github.com/hantsy/spring-reactive-sample/tree/master/cors) example this class is deliberately not registered as a Spring configuration — annotate it with `@Configuration` (or `@Import` it from `WebConfig`) to activate it in place of the `CorsWebFilter` bean.

## Spring Boot customization

Spring Boot 4 has no `spring.webflux.cors.*` properties; CORS is customized with beans and annotations. A `CorsWebFilter` bean is detected automatically, and the rules can be scoped to specific paths:

```java
@Configuration(proxyBeanMethods = false)
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://allowed-origin.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/posts/**", config);
        return new CorsWebFilter(source);
    }
}
```

For individual handlers, `@CrossOrigin` declares the allowed origins directly on the controller method:

```java
@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    @CrossOrigin(origins = "https://hello-origin.com")
    public Mono<String> hello() {
        return Mono.just("Hello CORS");
    }
}
```

!!! note

    Keep filter-level and annotation-level rules on distinct paths when demonstrating both. In `boot-cors` the filter covers `/posts/**` while `@CrossOrigin` applies to `/hello`, so each mechanism can be observed independently.

## Testing with WebTestClient

A preflight test asserts the negotiated headers:

```java
this.rest
        .options().uri("/posts")
        .header(HttpHeaders.ORIGIN, "https://allowed-origin.com")
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://allowed-origin.com")
        .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
        .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
```

A disallowed origin is rejected for both preflight and actual requests:

```java
this.rest
        .get().uri("/posts")
        .header(HttpHeaders.ORIGIN, "https://disallowed-origin.com")
        .exchange()
        .expectStatus().isForbidden();
```

!!! note

    As of Spring Framework 7.1, the CORS processor validates the `Origin` against the request URI's scheme, host, and port. When binding `WebTestClient` to an application context, set a base URL such as `.baseUrl("http://localhost:8080")` — otherwise the mock request has no scheme or host and every CORS request is rejected as a malformed origin. Tests against a running server (`@SpringBootTest` with `RANDOM_PORT`) are not affected.

For the complete code, see [cors](https://github.com/hantsy/spring-reactive-sample/tree/master/cors) and [boot-cors](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-cors) in this repo.