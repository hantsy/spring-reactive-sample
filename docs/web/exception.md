---
sort: 4
---

# Handing Web Exceptions

We have know when exposing RESTful APIs, we can choose between `@RestController` and `RouterFunction`.

The former is simple. If you have some experience with Spring WebMvc, it is easy to update your knowledge.  Just need to use newer Reactor specific `Mono` and `Flux`  instead in your codes.

The exception handling in *reactive* `@RestController` is similar to the one in WebMVC.

1. Create an `Exception` class.

   ```java
   public class PostNotFoundException extends RuntimeException {
       public PostNotFoundException(Long id) {
           super("Post:" + id +" is not found.");
       }
   }
   ```

   

2. Throw exception in your codes.

   ```java
   @GetMapping(value = "/{id}")
   public Mono<Post> get(@PathVariable(value = "id") Long id) {
       return this.posts.findById(id).switchIfEmpty(Mono.error(new PostNotFoundException(id)));
   }
   ```

   

3. Create a standalone `@ControllerAdvice` annotated class or `@ExceptionHandler` annotated method in your controller class to handle the exceptions.

   ```java
   @RestControllerAdvice
   @Slf4j
   class RestExceptionHandler {

       @ExceptionHandler(PostNotFoundException.class)
       ResponseEntity postNotFound(PostNotFoundException ex) {
           log.debug("handling exception::" + ex);
           return notFound().build();
       }

   }
   ```

`@RestControllerAdvice` only works for `@RestController` .

If you are using `RouterFunction`, create a  `WebExceptionHandler` bean to handle it manually.

```java
@Bean
public WebExceptionHandler exceptionHandler() {
    return (ServerWebExchange exchange, Throwable ex) -> {
        if (ex instanceof PostNotFoundException) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }
        return Mono.error(ex);
    };
}
}
```

> NOTE:  `WebExceptionHandler` is a low-level API, also works well for the exception handling  in the former controller case.

For the complete codes,  check  [spring-reactive-sample/exception-handler](https://github.com/hantsy/spring-reactive-sample/blob/master/exception-handler) and [spring-reactive-sample/boot-exception-handler](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-exception-handler) .







