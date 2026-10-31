
# Multipart Support

In Spring, the Servlet stack supports multipart requests through the Servlet 3.0+ built-in multipart feature or Apache Commons IO. The WebFlux stack also adds multipart support — backed by non-blocking stream processing instead of buffering whole files in memory.

The `Part` abstraction represents a part in a `multipart/form-data` request. A part is either a `FilePart` (uploaded file content) or a `FormFieldPart` (a simple form field), whether the request comes from a browser form or any other client.

## Reading parts in a controller

The reactive controllers accept `Part` parameters wrapped in `Mono` or `Flux`. The following variants are supported:

```java
@PostMapping("/requestBodyMap")
Mono<String> requestBodyMap(
    @RequestPart("fileParts") FilePart fileParts,
    @RequestPart("fileParts") Mono<FilePart> filePartsMono,
    @RequestPart("fileParts") Flux<FilePart> filePartsFlux,
    @RequestBody Mono<MultiValueMap<String, Part>> partsMono) {
    // ...
}
```

For a streaming approach, consume all parts as a `Flux<Part>` — this is the variant the sample implements:

```java
@RestController
@RequestMapping("/uploads")
public class MultipartController {

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Mono<String> requestBodyFlux(@RequestBody Flux<Part> parts) {
        return partFluxDescription(parts);
    }

    private static Mono<String> partFluxDescription(Flux<? extends Part> partsFlux) {
        return partsFlux.log().collectList().map(MultipartController::partListDescription);
    }

    private static String partDescription(Part part) {
        return part instanceof FilePart fp ? part.name() + ":" + fp.filename() : part.name();
    }

    // partListDescription(...) joins the part descriptions into a single string
}
```

For the complete codes, check [spring-reactive-sample/multipart](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart).

## File upload and download with Mongo GridFS

A step further — and closer to a real world application — is to store uploads in MongoDB via `ReactiveGridFsTemplate` and stream them back for download.

### Step 1: declare a ReactiveGridFsTemplate bean

```java
@Bean
public ReactiveGridFsTemplate reactiveGridFsTemplate() throws Exception {
    return new ReactiveGridFsTemplate(reactiveMongoDbFactory(), mappingMongoConverter());
}
```

### Step 2: handle upload and download in a controller

```java
@RestController()
@RequestMapping(value = "/multipart")
@RequiredArgsConstructor
public class MultipartController {

    private final ReactiveGridFsTemplate gridFsTemplate;

    @PostMapping("")
    public Mono<ResponseEntity> upload(@RequestPart Mono<FilePart> fileParts) {
        return fileParts
            .flatMap(part -> this.gridFsTemplate.store(part.content(), part.filename()))
            .map((id) -> ok().body(Map.of("id", id.toHexString())));
    }

    @GetMapping("{id}")
    public Flux<Void> read(@PathVariable String id, ServerWebExchange exchange) {
        return this.gridFsTemplate.findOne(query(where("_id").is(id)))
            .log()
            .flatMap(gridFsTemplate::getResource)
            .flatMapMany(r -> exchange.getResponse().writeWith(r.getDownloadStream()));
    }
}
```

In the above codes, `upload` stores the `FilePart` content in GridFS and returns the generated id to the client. The `read` method looks the file up by id and writes its content into the web response.

### Step 3: build multipart requests on the client

The `MultipartBodyBuilder` assembles multipart bodies on the client side. The following test uploads a file and reads it back:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class DemoApplicationTests {

    @LocalServerPort
    private int port;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + this.port)
            .build();
    }

    private MultiValueMap<String, HttpEntity<?>> generateBody() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("fileParts", new ClassPathResource("/foo.txt", DemoApplicationTests.class));
        return builder.build();
    }

    @Test
    public void testUpload() throws IOException {
        byte[] result = client
            .post()
            .uri("/multipart")
            .bodyValue(generateBody())
            .exchange()
            .expectStatus().isOk()
            .expectBody().returnResult().getResponseBody();

        ObjectMapper objectMapper = new ObjectMapper();
        Map bodyMap = objectMapper.readValue(result, Map.class);

        String fileId = (String) bodyMap.get("id");
        log.debug("updated file id:" + fileId);
        assertNotNull(fileId);

        client
            .get()
            .uri("/multipart/{id}", fileId)
            .exchange()
            .expectStatus().isOk();
    }
}
```

For the complete codes, check [spring-reactive-sample/multipart-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/multipart-data-mongo) and [spring-reactive-sample/boot-data-mongo-gridfs](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-gridfs).
