
# API Versioning

Spring Framework 7 adds built-in API versioning support on both the server and the client side. The version can be carried in a URI path segment, a request parameter, an HTTP header, or a media type parameter, and routes can be mapped per version with a `version` attribute on the mapping annotations.

This page covers the **reactive (WebFlux)** flavor; the same concepts apply to the Servlet stack. The samples referenced here are [api-versioning-webflux](https://github.com/hantsy/spring7-sandbox/tree/main/api-versioning-webflux) and [boot-api-versioning-webflux](https://github.com/hantsy/spring7-sandbox/tree/main/boot-api-versioning-webflux) in the spring7-sandbox repository — see its [API Versioning guide](https://github.com/hantsy/spring7-sandbox/blob/main/docs/api-versioning.md) for the full treatment including WebMVC.

## Server-side configuration

Implement `WebFluxConfigurer` and override `configureApiVersioning` to declare where the version is read from and which versions are supported:

```java
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                //.usePathSegment(0)
                .useRequestHeader("X-API-Version")
                //.useRequestParam("version")

                .setDefaultVersion("1.0")
                // When a defaultVersion is also set, this is automatically set to false.
                // .setVersionRequired(true)

                // default is SemanticApiVersionParser
                //.setVersionParser(new SemanticApiVersionParser())

                // set detectSupportedVersions(false) when adding supported versions
                .detectSupportedVersions(false)
                .addSupportedVersions("1.0", "1.1", "2.0");
    }
}
```

The version resolution strategies, exactly one of which is active at a time:

- `usePathSegment(0)` — read the version from a URI path segment, for example `/api/v1/resource`
- `useRequestHeader("X-API-Version")` — read it from an HTTP header, for example `X-API-Version: 1`
- `useRequestParam("version")` — read it from a request parameter, for example `/api/resource?version=1`
- `useMediaTypeParameter(MediaType.APPLICATION_JSON, "version")` — append a version parameter to the `Accept` header value, for example `application/json;version=1`

Versions are parsed with `SemanticApiVersionParser` by default, and `setDefaultVersion("1.0")` handles requests that carry no version at all.

## Versioned routes

Mapping annotations accept a `version` attribute. Requests without a version map to the unversioned method; requests carrying a version map to the method that declares it:

```java
@RestController
@RequestMapping("/hello")
public class GreetingController {

    @GetMapping()
    public String helloDefault() {
        return "Hello v1.0(Default)";
    }

    @GetMapping(version = "1.1")
    public String helloV1_1() {
        return "Hello v1.1";
    }

    @GetMapping(version = "2.0")
    public String helloV2_0() {
        return "Hello v2.0";
    }
}
```

In a Spring Boot 4 WebFlux application, the same `WebConfig` applies, and Boot exposes properties for it — the WebMVC counterpart reads `spring.mvc.api-versioning.*` (see the [spring7-sandbox guide](https://github.com/hantsy/spring7-sandbox/blob/main/docs/api-versioning.md) for the full property list).

## Client-side versioning with WebClient

`WebClient` participates in versioning through `ApiVersionInserter` — it declares where the version is written — and `defaultApiVersion` sets a fallback for requests that do not name one:

```java
this.client = WebClient.builder()
        .baseUrl("http://localhost:" + this.port)
        .defaultApiVersion("1.0")
        .apiVersionInserter(ApiVersionInserter.builder()
                .useHeader("X-API-Version")
                //.usePathSegment(0)
                //.useQueryParam("version")
                //.withVersionFormatter(ApiVersionFormatter)
                .build()
        )
        .build();
```

The inserter strategies mirror the server side — `useHeader`, `usePathSegment`, `useQueryParam`, or `withVersionFormatter` for custom rendering — and must agree with how the server resolves versions.

Per-request versions override the default:

```java
this.client.get().uri("/hello")
        .apiVersion("1.1")
        .retrieve()
        .bodyToMono(String.class)
```

A full integration test drives all three versions against the running server:

```java
@SpringJUnitConfig(classes = Application.class)
public class IntegrationTests {

    @Autowired
    HttpServer httpServer;

    @BeforeEach
    public void setup() {
        this.disposableServer = this.httpServer.bindNow(Duration.ofMillis(5_000));
        this.client = WebClient.builder()
                .baseUrl("http://localhost:" + this.port)
                .defaultApiVersion("1.0")
                .apiVersionInserter(ApiVersionInserter.builder()
                        .useHeader("X-API-Version")
                        .build())
                .build();
    }

    @Test
    public void testHello1_1() {
        this.client.get().uri("/hello")
                .apiVersion("1.1")
                .retrieve()
                .bodyToMono(String.class)
                .as(StepVerifier::create)
                .expectNext("Hello v1.1")
                .verifyComplete();
    }
}
```

## Testing with WebTestClient

`WebTestClient` supports the same contract. In a `@WebFluxTest` slice, register a `WebTestClientBuilderCustomizer` (in Boot 4 it lives in the `org.springframework.boot.webtestclient.autoconfigure` package) to configure the versioning on the test client:

```java
@WebFluxTest(controllers = GreetingController.class)
public class GreetingControllerTest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        WebTestClientBuilderCustomizer testClientBuilderCustomizer() {
            return builder -> builder.defaultApiVersion("1.0")
                    .apiVersionInserter(ApiVersionInserter.useHeader("X-API-Version"))
                    .build();
        }
    }

    @Autowired
    WebTestClient webTestClient;

    @Test
    void testHello1_1() {
        this.webTestClient.get().uri("/hello")
                .apiVersion("1.1")
                .exchange()
                .expectBody(String.class).isEqualTo("Hello v1.1");
    }
}
```

For the complete codes, check [api-versioning-webflux](https://github.com/hantsy/spring7-sandbox/tree/main/api-versioning-webflux) (plain Spring) and [boot-api-versioning-webflux](https://github.com/hantsy/spring7-sandbox/tree/main/boot-api-versioning-webflux) (Spring Boot) in the spring7-sandbox repository.
