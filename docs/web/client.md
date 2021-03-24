---
sort: 5
---

# WebClient 

Similar to `RestTemplate` and `AsyncRestTemplate`, in the  WebFlux stack, Spring adds a `WebClient` to perform HTTP requests and interact with HTTP APIs. 

The following is a simple example of using `WebClient` to send a `GET` request to the  `/posts` URI and retrieve posts.

```java
WebClient client = WebClient.create("http://localhost:8080");
client
	.get()
	.uri("/posts")
	.exchange()
	.flatMapMany(res -> res.bodyToFlux(Post.class))
	.log()
	.subscribe(post -> System.out.println("post: " + post));
```

The `WebClient` provides a builder to configure `ClientConnector`, `Codecs`, `ExchangeStrategies`  and change `HttpHeaders`.  For example.

```java
WebClient client = WebClient.builder()
    //see: https://github.com/jetty-project/jetty-reactive-httpclient
    //.clientConnector(new JettyClientHttpConnector())
    .clientConnector(new ReactorClientHttpConnector())
    .codecs(
    clientCodecConfigurer ->{
        // use defaultCodecs() to apply DefaultCodecs
        // clientCodecConfigurer.defaultCodecs();

        // alter a registered encoder/decoder based on the default config.
        // clientCodecConfigurer.defaultCodecs().jackson2Encoder(...)

        // Or
        // use customCodecs to register Codecs from scratch.
        clientCodecConfigurer.customCodecs().register(new Jackson2JsonDecoder());
        clientCodecConfigurer.customCodecs().register(new Jackson2JsonEncoder());
    }

)
    .exchangeStrategies(ExchangeStrategies.withDefaults())
    //                .exchangeFunction(ExchangeFunctions.create(new ReactorClientHttpConnector())
    //                        .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {})))
    //                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {clientRequest.}))
    //.defaultHeaders(httpHeaders -> httpHeaders.addAll())
    .baseUrl("http://localhost:8080")
    .build();

```

By default, the `ClientConnector` is `ReactorClientHttpConnector`, there are some other built-in implementations:

* [Jetty Reactive HttpClient](https://github.com/jetty-project/jetty-reactive-httpclient)
* [Apache HttpComponents](https://hc.apache.org/index.html)

For the complete codes, check [spring-reactive-sample/client](https://github.com/hantsy/spring-reactive-sample/blob/master/client).

Another client utility class is `WebTestClient`  , which is used for testing purpose.

The following is a simple example.

```java
@WebFluxTest(controllers = MessageController.class)
public class DemoApplicationTests {

    @Autowired
    WebTestClient client;

    @Test
    public void getAllMessagesShouldBeOk() {
        client.get().uri("/").exchange().expectStatus().isOk();
    }

}
```

The usage of the `WebTestClient` API is very similar to `WebClient`, but provides  methods to assert the response result.

> NOTE: Although `WebClient` and `WebTestClient` looks similar, but `WebTestClient` is not derived from `WebClient`.

