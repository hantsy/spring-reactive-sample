
# Spring RestDocs

[Spring RestDocs](https://spring.io/projects/spring-restdocs) generates accurate API documentation from tests. It integrates with `WebTestClient`, so every request a test performs contributes a documented snippet.

## Add the test dependencies

```xml
<dependency>
    <groupId>org.springframework.restdocs</groupId>
    <artifactId>spring-restdocs-webtestclient</artifactId>
    <scope>test</scope>
</dependency>
```

The sample also wires the `spring-restdocs-asciidoctor` artifact into the Asciidoctor Maven plugin to assemble the snippets into a document.

## Document a request in a test

Register the `RestDocumentationExtension`, configure the client with `documentationConfiguration`, and add the `document(...)` filter to each call you want documented:

```java
@SpringJUnitConfig(classes = Application.class)
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
public class ApiDocumentation {

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentation) {
        this.client = WebTestClient
            .bindToApplicationContext(this.context)
            .configureClient()
            .baseUrl("https://api.example.com")
            .filter(documentationConfiguration(restDocumentation))
            .build();
    }

    @Test
    public void getAllPostsWillBeOk() throws Exception {
        this.client
            .get()
            .uri("/posts")
            .exchange()
            .expectStatus().isOk()
            .expectBody().consumeWith(document("sample"));
    }
}
```

Running the test produces the snippets under `target/generated-snippets/sample/` — `httpie-request.adoc`, `curl-request.adoc`, `response-body.adoc`, and friends — which the Asciidoctor build then composes into the published API reference.

For the complete codes, check [spring-reactive-sample/restdocs](https://github.com/hantsy/spring-reactive-sample/tree/master/restdocs).
