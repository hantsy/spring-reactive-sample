
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

## Streaming with PartEvent

Beyond the aggregated forms above, WebFlux can consume a multipart request as a stream of `PartEvent` messages — one event per form field and a pair of events (start/content) per file part. This processes parts as they arrive instead of buffering them, and scales to large uploads.

From the [multipart](https://github.com/hantsy/spring6-sandbox/tree/main/multipart) sample of the spring6-sandbox repository, the controller consumes `Flux<PartEvent>`, splits the stream at each part boundary with `windowUntil(PartEvent::isLast)`, and dispatches on the event type:

```java
@PostMapping("partevents")
public ResponseEntity<Flux<String>> handlePartsEvents(@RequestBody Flux<PartEvent> allPartsEvents) {
    var result = allPartsEvents
            .windowUntil(PartEvent::isLast)
            .concatMap(p -> {
                        return p.switchOnFirst((signal, partEvents) -> {
                                    if (signal.hasValue()) {
                                        PartEvent event = signal.get();
                                        if (event instanceof FormPartEvent formEvent) {
                                            String value = formEvent.value();
                                            // handle form field
                                            log.debug("form value: {}", value);
                                            return Mono.just(value + "
");

                                        } else if (event instanceof FilePartEvent fileEvent) {
                                            String filename = fileEvent.filename();
                                            log.debug("upload file name:{}", filename);

                                            return partEvents.map(PartEvent::content)
                                                    .map(DataBufferUtils::release)
                                                    .then(Mono.just(filename + "
"));
                                        }

                                        return Mono.error(new RuntimeException("Unexpected event: " + event));
                                    }
                                    return Flux.empty(); // either complete or error signal
                                }
                        );
                    }
            );

    return ok().body(result);
}
```

Reading the flow:

1. `windowUntil(PartEvent::isLast)` groups the event stream into windows, one per part.
2. `switchOnFirst` inspects the first event of each window: a `FormPartEvent` carries the field value directly, a `FilePartEvent` opens the file — the content events that follow stream the file data (`partEvents.map(PartEvent::content)`), where you would write to storage instead of releasing the buffers.
3. `DataBufferUtils::release` frees the buffer memory once the content is consumed.

The controller also demonstrates the other multipart consumption styles — a form command object, `@RequestPart` parameters, and `Mono<MultiValueMap<String, Part>>`:

```java
@PostMapping("form")
public ResponseEntity handleFileUploadForm(FileUploadCommand form, BindingResult errors) {
    var result = Map.of(
            "name", form.getName(),
            "filename", form.getFile().filename()
    );
    return ok().body(result);
}

@PostMapping("requestparts")
public ResponseEntity handleRequestParts(@RequestPart("name") String name,
                                         @RequestPart("file") FilePart file) {
    var result = Map.of(
            "name", name,
            "filename", file.filename()
    );
    return ok().body(result);
}

@PostMapping("multivalues")
public ResponseEntity handleMultivalues(@RequestBody Mono<MultiValueMap<String, Part>> parts) {
    var partNames = parts.map(p -> p.keySet().stream().map(key -> p.getFirst(key).name()).toList()).log();
    return ok().body(partNames);
}
```

A test builds the `PartEvent` stream on the client side with `FormPartEvent.create(...)` and `FilePartEvent.create(...)`:

```java
@Test
public void testHandlePartEvents() {
    this.client
            .post().uri("/partevents")
            .contentType(MULTIPART_FORM_DATA)
            .body(
                    Flux.concat(
                            FormPartEvent.create("name", "test"),
                            FilePartEvent.create("file", new ClassPathResource("spring.png"))
                    ),
                    PartEvent.class
            )
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(String.class).hasSize(2);
}
```

For the complete codes, check the [multipart](https://github.com/hantsy/spring6-sandbox/tree/main/multipart) module of the spring6-sandbox repository.

## File upload and download

For file upload/download backed by a store, stream the `FilePart` content into the backend of your choice. The [Mongo GridFS example](../data/mongo.md#gridfs-file-upload-and-download) stores uploads in MongoDB via `ReactiveGridFsTemplate` and streams them back on download; the [R2DBC FilePart example](https://github.com/hantsy/spring-r2dbc-sample/tree/master/boot-filepart) maps upload content to Postgres `bytea` (`byte[]`, `ByteBuffer`, or R2DBC `Blob`).
