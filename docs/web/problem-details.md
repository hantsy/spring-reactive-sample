# Problem Details

When building REST API backends, developers often invent custom wrappers such as `ApiResult` or `ErrorResponse` to standardize responses. Those formats are rarely portable, and consuming a different one for every third-party API quickly becomes frustrating.

[Spring HATEOAS](https://spring.io/projects/spring-hateoas) adopted the [vnd.error draft proposal](https://github.com/blongden/vnd.error) for representing errors, but it is primarily aimed at hypermedia APIs.

The widely accepted cross-language alternative is **Problem Details for HTTP APIs**, standardized by the IETF as [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (originally RFC 7807). It defines a consistent, human-readable structure for error conditions so that clients can interpret errors uniformly. It is served as media type `application/problem+json` (or `application/problem+xml`).

Spring 6 added native support for Problem Details and Spring Boot builds on it, so there is usually no reason to hand-roll an error envelope any more.

## The `ProblemDetail` class

[`ProblemDetail`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ProblemDetail.html) is a POJO that models the RFC 9457 members:

| Member | Description |
| :--- | :--- |
| `type` | A URI reference identifying the problem type. Defaults to `about:blank` when omitted. |
| `title` | A brief, human-readable summary of the problem type. |
| `status` | The HTTP status code. |
| `detail` | A human-readable explanation specific to this occurrence. |
| `instance` | A URI reference identifying this specific occurrence, usually the request path. |
| `properties` | An extension point for adding custom members. |

Create an instance with one of the factory methods:

```java
ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Post was not found.");
```

Any additional data goes into the `properties` map:

```java
var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request is invalid.");
problemDetail.setProperty("errors",
        List.of(Map.of("path", "title", "details", "Cannot be empty"))
);
```

When Jackson is used, Spring registers `ProblemDetailJacksonMixin`; the entries of `properties` are written as top-level JSON members instead of being nested under a `properties` key:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "The request is invalid.",
  "instance": "/posts",
  "errors": [
    { "path": "title", "details": "Cannot be empty" }
  ]
}
```

## Producing Problem Details

In both Spring WebMvc and WebFlux, error responses can be assembled in `@ExceptionHandler` methods within a `@ControllerAdvice` / `@RestControllerAdvice` bean. The handler may return `ProblemDetail` directly, or wrap it in a `ResponseEntity<ProblemDetail>` when it needs to control headers or the response status explicitly:

```java
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(PostNotFoundException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setProperty("id", exception.getPostId());
        problemDetail.setProperty("entity", "POST");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
```

The response is rendered with `Content-Type: application/problem+json`, and the response status is taken from the `ProblemDetail`.

## Problem Details in a manual WebFlux application

The sample project [problem-details](https://github.com/hantsy/spring-reactive-sample/tree/master/problem-details) bootstraps Spring WebFlux without Spring Boot, stores posts in PostgreSQL with R2DBC, and returns `ProblemDetail` bodies for errors.

A domain exception can carry the extra data that should be exposed:

```java
public class PostNotFoundException extends RuntimeException {

    private final UUID postId;

    public PostNotFoundException(UUID postId) {
        super("Post #" + postId + " was not found.");
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }
}
```

The controller converts an empty result into that exception:

```java
@GetMapping("{id}")
public Mono<ResponseEntity<Post>> get(@PathVariable UUID id) {
    return this.posts.findById(id)
            .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
            .map(ResponseEntity::ok);
}
```

Then a `@RestControllerAdvice` maps it to a `ProblemDetail` and adds extension members:

```java
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(PostNotFoundException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setType(URI.create("https://example.com/errors/post-not-found"));
        problemDetail.setProperty("id", exception.getPostId());
        problemDetail.setProperty("entity", "POST");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
}
```

A request for a missing post returns:

```json
{
  "type": "https://example.com/errors/post-not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "Post #4c2f9f0c-2b8a-5d1e-4c73-7f1b3c9a8f6e was not found.",
  "instance": "/posts/4c2f9f0c-2b8a-5d1e-4c73-7f1b3c9a8f6e",
  "id": "4c2f9f0c-2b8a-5d1e-4c73-7f1b3c9a8f6e",
  "entity": "POST"
}
```

### Handling validation errors

`@Valid` on a `@RequestBody` argument raises a `WebExchangeBindException` in WebFlux. Extend `ResponseEntityExceptionHandler` and override `handleWebExchangeBindException` to enrich the framework-provided `ProblemDetail` with the field errors:

```java
@Override
protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(
        WebExchangeBindException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        ServerWebExchange exchange) {
    return super.handleWebExchangeBindException(exception, headers, status, exchange)
            .map(entity -> {
                if (entity.getBody() instanceof ProblemDetail problemDetail) {
                    problemDetail.setProperty("errors",
                            exception.getFieldErrors().stream()
                                    .map(error -> Map.of(
                                            "path", error.getField(),
                                            "details", error.getDefaultMessage()))
                                    .toList()
                    );
                }
                return entity;
            });
}
```

A validation failure now includes an `errors` extension member:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid request content.",
  "instance": "/posts",
  "errors": [
    { "path": "content", "details": "must not be blank" }
  ]
}
```

## Problem Details in a Spring Boot application

Enable the built-in support in `application.properties`:

```properties
# For Spring WebMvc
spring.mvc.problemdetails.enabled=true

# For Spring WebFlux
spring.webflux.problemdetails.enabled=true
```

Spring Boot then registers a `ProblemDetailsExceptionHandler` that converts framework exceptions such as `ResponseStatusException`, `MethodArgumentNotValidException`, and `MethodArgumentTypeMismatchException` into Problem Details responses. The built-in error handling also renders errors in the same format.

Application-specific exceptions can still be customized with a `@RestControllerAdvice`. Give it the highest precedence so it runs before Spring Boot's advice, and only handle the exceptions you care about — everything else falls through to the built-in handler:

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(PostNotFoundException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setType(URI.create("https://example.com/errors/post-not-found"));
        problemDetail.setProperty("id", exception.getPostId());
        problemDetail.setProperty("entity", "POST");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleWebExchangeBindException(WebExchangeBindException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "The request is invalid.");
        problemDetail.setProperty("errors",
                exception.getFieldErrors().stream()
                        .map(error -> Map.of(
                                "path", error.getField(),
                                "details", error.getDefaultMessage()))
                        .toList()
        );
        return ResponseEntity.badRequest().body(problemDetail);
    }
}
```

The Boot variant, [boot-problem-details](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-problem-details), uses Spring Data R2DBC instead of a hand-written `DatabaseClient` repository:

```java
public interface PostRepository extends ReactiveCrudRepository<Post, UUID> {

    Flux<Post> findByTitleContains(String title);
}
```

Because of the built-in handler, an invalid path variable is also converted into a Problem Details response without any application code:

```
GET /posts/not-a-uuid
```

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Failed to convert 'id' with value: 'not-a-uuid'",
  "instance": "/posts/not-a-uuid"
}
```

## Testing with `WebTestClient`

`WebTestClient` exposes the response status, headers, and body, so the media type and extension members can be asserted directly:

```java
this.client
        .get().uri("/posts/{id}", postId)
        .exchange()
        .expectStatus().isNotFound()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.type").isEqualTo("https://example.com/errors/post-not-found")
        .jsonPath("$.id").isEqualTo(postId.toString())
        .jsonPath("$.entity").isEqualTo("POST");
```

## Alternative: Zalando Problem

Before Spring 6, applications that wanted the Problem Details format typically used [Zalando Problem](https://github.com/zalando/problem) together with its [Spring Web integration](https://github.com/zalando/problem-spring-web), which supports both Spring WebMvc and WebFlux.

!!! note

    Zalando's library still offers deeper integration with the Spring ecosystem than the built-in support — for example it handles Spring Security exceptions and Jakarta Validation errors automatically. It remains a reasonable choice for applications that need those integrations out of the box.

## Source code

* [problem-details](https://github.com/hantsy/spring-reactive-sample/tree/master/problem-details) — manual Spring WebFlux + R2DBC application.
* [boot-problem-details](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-problem-details) — Spring Boot + Spring Data R2DBC application.
