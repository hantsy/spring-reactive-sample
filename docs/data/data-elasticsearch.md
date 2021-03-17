---
sort: 8
---


# Spring Data ElasticSearch

ElasticSearch is well known as a search engine, also working well as document based NoSQL.

Spring Data ElasticSearch adds basic Reactive support.

Generate a project skeleton from [Spring Intializr](https://start.spring.io).

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

Create a `Post` class to present a document in ElasticSearch, and add a `@Document` annotation on the class level.

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
class Post {

    @Id
    private String id;
    private String title;

    @Field("content")
    private String content;

}
```

Create `PostRepository` interface.

```java
interface PostRepository extends ReactiveElasticsearchRepository<Post, String> {
}
```

For the complete codes, check [spring-reactive-sample/boot-data-elasticsearch](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-elasticsearch).

Add some sample data via a `CommandLineRunner` bean or a `ApplicationRunner` bean.

```java
public void init() {
	log.info("start data initialization  ...");
	this.posts
		.deleteAll()
		.thenMany(
			Flux
				.just("Post one", "Post two")
				.flatMap(
					title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
				)
		)
		.log()
		.subscribe(
			null,
			null,
			() -> log.info("done initialization...")
		);

}
```

To run the application, you have to serve a running ElasticSearch server firstly.

```bash
docker-compose up elasticsearch
```




