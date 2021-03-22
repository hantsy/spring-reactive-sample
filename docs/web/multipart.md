---
sort: 6
---

# Multipart Support 

In Spring, the Servlet stack support Multipart by Servlet 3.0+ built-in multipart feature or Apache Commons IO. The new WebFlux stack also add Multipart support.

The `Part` presents a part in a `multipart/form-data` request, it could be a `FilePart` or`FormFieldPart` in a browser or any content type outside of browser .

Like the general `@Controller`, it accepts a reactive `Part` parameter  wrapped with `Mono` or `Flux`.

For example,  the following method demonstrates the possible parameters can be processed in WebFlux.

```java
@PostMapping("/requestBodyMap")
Mono<String> requestBodyMap(
    @RequestPart("fileParts") FilePart fileParts,
	@RequestPart("fileParts") Mono<FilePart> filePartsMono,
	@RequestPart("fileParts") Flux<FilePart> filePartsFlux,
    @RequestBody Mono<MultiValueMap<String, Part>> partsMono) {
      
}
```

For the complete codes,  check  [spring-reactive-sample/multipart](https://github.com/hantsy/spring-reactive-sample/blob/master/multipart).

Now let's go to a more complex example *File upload and download* - which is more close to the real world application.  

1. Create a  Mongo `GridfsTemplate` bean.

   ```java
   @Bean
   public ReactiveGridFsTemplate reactiveGridFsTemplate() throws Exception {
       return new ReactiveGridFsTemplate(reactiveMongoDbFactory(), mappingMongoConverter());
   }
   ```
   
2. Create a `Controller` to handle upload and download.
   
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

   In the above codes, `upload` is used for uploading, the `gridFstemplate` store the `filePart` content and return the id to client. The `read` method reads content from Mongo according the provided id, write the content into the web response.
   
3. There is a `MultipartBodyBuilder` can be used to build multipart in client. The following is an example use `MultipartBodyBuilder` in testing codes.
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
   

For the complete codes, check [spring-reactive-sample/multipart-data-mongo](https://github.com/hantsy/spring-reactive-sample/blob/master/multipart-data-mongo) and [spring-reactive-sample/boot-data-mongo-gridfs](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-mongo-gridfs).  

