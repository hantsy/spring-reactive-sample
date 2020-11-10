# Data Auditing with Spring Data Neo4j

We have [introduced the Data Auditing feature](https://medium.com/swlh/data-auditing-with-spring-data-r2dbc-5d428fc94688) in the latest Spring Data R2dbc 1.2. Similarly Spring Data Neo4j added the reactive data auditing support as what is done in Spring Data R2dbc.

Let's reuse the former example we have done in [the last post](./data-neo4j.md).

Change the original `Post` entity, add the following fields to capture the timestamp and auditor when saving and updating the entity.

```java
@Node
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

	//...

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
```

In the above codes, there are some annotations applied on the fields. 

*  `CreatedDate` will fill the current date when saving the entity.
* `LastModifiedDate` will fill the current date when updating the entity.
* `CreatedBy` will retrieve the current auditor from  `ReactiveAuditorAware`  and fill it when saving the entity.
* `LastModifiedBy` will retrieve the current auditor from  `ReactiveAuditorAware`  and fill it when updating the entity.

Add a `ReactiveAuditorAware` bean to serve the auditor in the entity when saving and updating it.

```java
@Bean
public ReactiveAuditorAware<String> reactiveAuditorAware() {
    return () -> Mono.just("hantsy");
}
```

> Note : in the real world applications, you can retrieve the current user from current SecurityContextHolder.

Do not forget to add a `@EnableReactiveNeo4jAuditing`  annotation on the `@Configuration` class to activate the data auditing feature.

```java
@Configuration(proxyBeanMethods = false)
@EnableReactiveNeo4jAuditing
class DataConfig {

    @Bean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {... }
}
```

Run the application, you will see the following logging info printed by the `DataInitializer` bean.

 ```bash
2020-11-08 10:22:26.661  INFO 16856 --- [o4jDriverIO-2-2] [Initializing data]                      : onNext(Post(id=2, title=Post one, content=The content of Post one, createdDate=2020-11-08T10:22:24.554356100, updatedDate=2020-11-08T10:22:24.554356100, createdBy=hantsy, updatedBy=hantsy))
2020-11-08 10:22:26.661  INFO 16856 --- [o4jDriverIO-2-2] com.example.demo.DataInitializer         : found post: Post(id=2, title=Post one, content=The content of Post one, createdDate=2020-11-08T10:22:24.554356100, updatedDate=2020-11-08T10:22:24.554356100, createdBy=hantsy, updatedBy=hantsy)
2020-11-08 10:22:26.661  INFO 16856 --- [o4jDriverIO-2-2] [Initializing data]                      : onNext(Post(id=3, title=Post two, content=The content of Post two, createdDate=2020-11-08T10:22:24.562356700, updatedDate=2020-11-08T10:22:24.562356700, createdBy=hantsy, updatedBy=hantsy))
2020-11-08 10:22:26.661  INFO 16856 --- [o4jDriverIO-2-2] com.example.demo.DataInitializer         : found post: Post(id=3, title=Post two, content=The content of Post two, createdDate=2020-11-08T10:22:24.562356700, updatedDate=2020-11-08T10:22:24.562356700, createdBy=hantsy, updatedBy=hantsy)
 ```

You can also verify it via `curl` .

```bash
# curl http://localhost:8080/posts
[{"id":2,"title":"Post one","content":"The content of Post one","createdDate":"2020-11-08T10:22:24.5543561","updatedDate":"2020-11-08T10:22:24.5543561","createdBy":"hantsy","updatedBy":"hantsy"},{"id":3,"title":"Post two","content":"The content of Post two","createdDate":"2020-11-08T10:22:24.5623567","updatedDate":"2020-11-08T10:22:24.5623567","createdBy":"hantsy","updatedBy":"hantsy"}]
```

Grab the [source code](http://github.com/hantsy/spring-reactive-sample) from my github.

