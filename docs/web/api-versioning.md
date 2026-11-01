
# API Versioning

API versioning is usually introduced only when an application has already grown enough to need contract changes without breaking older clients. Spring Framework 7 makes this easier by adding built-in versioning support on both the server and the client side.

A version can be carried in:

- a URI path segment
- a request parameter
- an HTTP header
- a media type parameter

The server can then route requests using a `version` attribute on the mapping annotations. This page looks at the WebFlux flavor of the feature.

The examples referenced here live in the spring7-sandbox repository: [api-versioning-webflux](https://github.com/hantsy/spring7-sandbox/tree/main/api-versioning-webflux) and [boot-api-versioning-webflux](https://github.com/hantsy/spring7-sandbox/tree/main/boot-api-versioning-webflux). See the [API Versioning guide](https://github.com/hantsy/spring7-sandbox/blob/main/docs/api-versioning.md) for the full WebMVC and WebFlux treatment.

## Server-side configuration

Implement `WebFluxConfigurer` and override `configureApiVersioning` to declare where the version is read from and which versions the service supports:

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

The version resolution strategies are mutually exclusive:

- `usePathSegment(0)` — read the version from a URI path segment, such as `/api/v1/resource`
- `useRequestHeader("X-API-Version")` — read it from an HTTP header, such as `X-API-Version: 1`
- `useRequestParam("version")` — read it from a query parameter, such as `/api/resource?version=1`
- `useMediaTypeParameter(MediaType.APPLICATION_JSON, "version")` — read it from the media type, such as `application/json;version=1`

`SemanticApiVersionParser` is the default parser, and `setDefaultVersion("1.0")` handles requests that do not specify a version at all.

## Versioned routes

Mapping annotations accept a `version` attribute. Requests without a version fall through to the default route, while versioned requests resolve to the matching method.

```java
@RestController
@RequestMapping("/hello")
public class GreetingController {

    @GetMapping
    public String helloDefault() {
        return "Hello v1.0 (default)";
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

In a Spring Boot 4 WebFlux application, the same configuration applies. Boot also exposes properties for this feature, while the WebMVC counterpart uses `spring.mvc.api-versioning.*` properties.

## Client-side versioning with WebClient

`WebClient` participates in the same strategy via `ApiVersionInserter`, and `defaultApiVersion` gives clients a fallback when a specific version is not requested:

```java
this.client = WebClient.builder()
        .baseUrl("http://localhost:" + this.port)
        .defaultApiVersion("1.0")
        .apiVersionInserter(ApiVersionInserter.builder()
                .useHeader("X-API-Version")
                //.usePathSegment(0)
                //.useQueryParam("version")
                //.withVersionFormatter(ApiVersionFormatter)
                .build())
        .build();
```

The client-side strategies mirror the server side: `useHeader`, `usePathSegment`, `useQueryParam`, or custom formatting with `withVersionFormatter`. The server and client must agree on the same versioning mechanism.

Per-request versions override the default:

```java
this.client.get().uri("/hello")
        .apiVersion("1.1")
        .retrieve()
        .bodyToMono(String.class);
```

A full integration test can drive all versions against the running server:

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

`WebTestClient` supports the same contract. In a `@WebFluxTest` slice, customize the client builder with the API versioning settings used by the app:

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

For the complete code, see [api-versioning-webflux](https://github.com/hantsy/spring7-sandbox/tree/main/api-versioning-webflux) and [boot-api-versioning-webflux](https://github.com/hantsy/spring7-sandbox/tree/main/boot-api-versioning-webflux) in the spring7-sandbox repo.
