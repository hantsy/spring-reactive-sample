
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

## File upload and download

For file upload/download backed by a store, stream the `FilePart` content into the backend of your choice. The [Mongo GridFS example](../data/mongo.md#gridfs-file-upload-and-download) stores uploads in MongoDB via `ReactiveGridFsTemplate` and streams them back on download; the [R2DBC FilePart example](https://github.com/hantsy/spring-r2dbc-sample/tree/master/boot-filepart) maps upload content to Postgres `bytea` (`byte[]`, `ByteBuffer`, or R2DBC `Blob`).
