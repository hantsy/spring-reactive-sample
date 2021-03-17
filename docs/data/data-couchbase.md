---
sort: 7
---

# Spring Data Couchbase


Spring Data Couchbase adds basic Reactive support.


Follow the the [Getting Started](./start) part to create a freestyle(none Spring Boot) or Spring Boot based project skeleton.

If you are not using Spring Boot, firstly add the following dependencies into your project.

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-couchbase</artifactId>
</dependency>
<dependency>
	<groupId>com.couchbase.client</groupId>
	<artifactId>java-client</artifactId>
</dependency>
<dependency>
	<groupId>io.reactivex</groupId>
	<artifactId>rxjava</artifactId>
</dependency>
<dependency>
	<groupId>io.reactivex</groupId>
	<artifactId>rxjava-reactive-streams</artifactId>
</dependency>
```

Create a `@Configuration` class to configure Cassandra and add `@EnableReactiveCouchbaseRepositories` to enable reactive `Repository` support.

```java
@Configuration
@EnableReactiveCouchbaseRepositories(basePackageClasses = {CouchbaseConfig.class})
public class CouchbaseConfig extends AbstractReactiveCouchbaseConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public String couchbaseAdminUser() {
        return env.getProperty("couchbase.adminUser", "Administrator");
    }

    @Bean
    public String couchbaseAdminPassword() {
        return env.getProperty("couchbase.adminPassword", "password");
    }

    @Override
    protected List<String> getBootstrapHosts() {
        return Collections.singletonList(env.getProperty("couchbase.host", "127.0.0.1"));
    }

    @Override
    protected String getBucketName() {
        return env.getProperty("couchbase.bucket", "default");
    }

    @Override
    protected String getBucketPassword() {
        return env.getProperty("couchbase.password", "");
    }

    @Override
    protected CouchbaseEnvironment getEnvironment() {
        return DefaultCouchbaseEnvironment.builder()
            .connectTimeout(10000)
            .kvTimeout(10000)
            .queryTimeout(10000)
            .viewTimeout(10000)
            .build();
    }

    @Override
    public RxJavaCouchbaseTemplate reactiveCouchbaseTemplate() throws Exception {
        RxJavaCouchbaseTemplate template = super.reactiveCouchbaseTemplate();
        template.setWriteResultChecking(WriteResultChecking.LOG);
        return template;
    }

    //this is for dev so it is ok to auto-create indexes
    @Override
    public IndexManager indexManager() {
        return new IndexManager();
    }

    @Override
    protected Consistency getDefaultConsistency() {
        return Consistency.READ_YOUR_OWN_WRITES;
    }

}
```

Create a `Post` class to present a document in Couchbase, and add a `@Document` annotation on the class level.

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
@ViewIndexed(designDoc = "post", viewName = "all")
interface PostRepository extends ReactiveCouchbaseRepository<Post, String>{}
```

For the complete codes, check [spring-reactive-sample/data-couchbase](https://github.com/hantsy/spring-reactive-sample/blob/master/data-couchbase).

Alternatively, for Spring Boot applications, just need to add `spring-boot-starter-data-couchbase-reactive` into your project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-couchbase-reactive</artifactId>
</dependency>
```

No need extra configuration, Spring Boot will auto-configure couchbase and registers all essential beans for you.

For the complete codes, check [spring-reactive-sample/boot-data-couchbase](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-couchbase).

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

To run the application, you have to serve a running Couchbase server firstly.

```bash
docker-compose up couchbase
```

And go to http://localhost:8091 to set the user account and bucket info.



