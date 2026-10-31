
# Groovy

Groovy interoperates with the Spring WebFlux stack out of the box: closures and the `.&` method-reference operator map naturally onto the functional routing API. The repository ships two Groovy samples, both plain Spring applications with manual wiring (see [Getting Started](../start/intro.md)).

## Functional routing in Groovy

The [groovy](https://github.com/hantsy/spring-reactive-sample/tree/master/groovy) sample builds the same blog API as the Java samples with a `RouterFunction` bean:

```groovy
@Configuration
@EnableWebFlux
class WebConfig {

    @Bean
    RouterFunction<ServerResponse> routes(PostHandler postHandler) {
        route(GET("/posts"), postHandler.&all)
                .andRoute(POST("/posts"), postHandler.&create)
                .andRoute(GET("/posts/{id}"), postHandler.&get)
                .andRoute(PUT("/posts/{id}"), postHandler.&update)
                .andRoute(DELETE("/posts/{id}"), postHandler.&delete)
    }
}
```

The `.&` operator creates a method reference from a Groovy method, exactly where Java uses the `::` operator. The handler class is an ordinary `@Component` with `Mono<ServerResponse>` methods, and the data layer reuses `ReactiveMongoRepository`:

```groovy
@Component
class PostHandler {

    private final PostRepository posts

    Mono<ServerResponse> all(ServerRequest req) {
        ok().body(this.posts.findAll(), Post)
    }

    Mono<ServerResponse> create(ServerRequest req) {
        req.bodyToMono(Post)
                .flatMap { this.posts.save(it) }
                .flatMap { p -> created(URI.create("/posts/${p.id}")).build() }
    }

    // get / update / delete follow the same pattern
}
```

The application class wires the server with the standard `WebHttpHandlerBuilder` + `ReactorHttpHandlerAdapter` combination and blocks on `main`.

## The Groovy bean definition DSL

The [groovy-dsl](https://github.com/hantsy/spring-reactive-sample/tree/master/groovy-dsl) sample declares beans in a Groovy script instead of annotations. The application imports the script with `@ImportResource`:

```groovy
@ComponentScan
@Configuration
@ImportResource("classpath:application.groovy")
class Application {
    // ...
}
```

And the script registers the beans in the `beans { }` DSL:

```groovy
import com.example.demo.DataInitializer
import com.example.demo.PostHandler
import com.example.demo.PostRepository

beans {

    postHandler(PostHandler){
        posts = ref("postRepository")
    }

    dataInitializer(DataInitializer){
        posts = ref("postRepository")
    }

    postRepository(PostRepository){
        template = ref("reactiveMongoTemplate")
    }
}
```

`ref("...")` resolves another bean by name, and property assignment wires collaborators — a concise alternative to XML or annotation configuration.

## Building with Groovy

Both samples compile Groovy with the GMavenPlus plugin:

```xml
<plugin>
    <groupId>org.codehaus.gmavenplus</groupId>
    <artifactId>gmavenplus-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

For the Spring Boot variant, see [boot-groovy](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-groovy).
