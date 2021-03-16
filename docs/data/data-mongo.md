---
sort: 4
---

# Spring Data Mongo

Spring Data Mongo provides reactive variants of `MongoTemplate` and `MongoRepository`, aka `ReactiveMongoTemplate` and `ReactiveMongoRepository` which have reactive capabilities.



## Getting Started

Follow the the [Getting Started](./start) part to create a freestyle or Spring Boot based project skeleton.

For a  Spring project, firstly add the following into project dependencies.

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-mongodb</artifactId>
</dependency>
<dependency>
	<groupId>org.mongodb</groupId>
	<artifactId>mongodb-driver-reactivestreams</artifactId>
</dependency>
```

Create a `@Configuration` class to configure Mongo and enable Reactive support.

```java
@EnableReactiveMongoRepositories(basePackageClasses = {MongoConfig.class})
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${mongo.uri}")
    String mongoUri;

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        return "blog";
    }

}
```

Create a new `Post` MongoDB document class.

```java
@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

    @Id
    private String id;
    private String title;
    private String content;


}
```

1. `@Document` declares it as a MongoDB document.
2. `@Id` indicates it is the identifier field of `Post` document.

Declares a `PostRepository` interface to extend Spring Data MongoDB specific `ReactiveMongoRepository`.

```java
interface PostRepository extends ReactiveMongoRepository<Post, String> {
}
```

Configure MongoDB connection in the *appliation.yml* file.

```yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/blog
```

Before starting up your application, make sure there is a running MongoDB instance in your local system. 

>**NOTE**: If you have not installed it, go to [Mongo download page](ttps://www.mongodb.com/download-center?jmp=nav#community) and get a copy of MongoDB, and install it into your system.

Alternatively, if you are familiar with Docker, it is simple to start a MongoDB instance via Docker Compose file.

```yml
version: '3.3' # specify docker-compose version

# Define the services/containers to be run
services:

  redis:
    image: redis
    ports:
      - "6379:6379"
      
  mongodb: 
    image: mongo 
    volumes:
      - mongodata:/data/db
    ports:
      - "27017:27017"
    command: --smallfiles --rest
#   command: --smallfiles --rest --auth  

volumes:
  mongodata:  
```

Execute the following command to start a Mongo instance in a Docker container.

```
docker-compose up mongodb
```

When the Mongo service is started, it is ready for bootstrapping the application.

```
mvn spring-boot:run
```

 For the complete codes, check [spring-reactive-sample/data-mongo](https://github.com/hantsy/spring-reactive-sample/blob/master/data-mongo).

If you are using Spring Boot, the configuration can be simplified. Just need to add `spring-boot-starter-data-mongodb-reactive` into the project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```

No need extra configuration class, Spring Boot will enable reactive support for MongoDB in this project. `ReactiveMongoTemplate` and `ReactiveMongoRepository` will be configured automatically.

 For the complete codes, check [spring-reactive-sample/boot-data-mongo](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-mongo).

Add some sample data into MongoDB when starting the application.

Declare a `CommandLineRunner` bean.

```java
@Component
@Slf4j
class DataInitializr implements CommandLineRunner {

    private final PostRepository posts;

    public DataInitializr(PostRepository posts) {
        this.posts = posts;
    }

    @Override
    public void run(String[] args) {
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

}
```

Use a `CommandLineRunner` to make sure the `run` method is executed after the application is started.

Execute `mvn spring-boot:run` to start up the application now, then we can test if the data is initialized successfully.

```
curl -v http://localhost:8080/posts
* timeout on name lookup is not supported
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /posts HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.1
> Accept: */*
>
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: application/json;charset=UTF-8
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Content-Type-Options: nosniff
< X-Frame-Options: DENY
< X-XSS-Protection: 1 ; mode=block
<
[{"id":"599149d53c44062e08c58b86","title":"Post one","content":"content of Post one","createdDate":[2017,8,14,14,57,25,71000000]},{"id":"599149d53c44062e08c58b87","title":"Post two","content":"content of Post two","createdDate":[2017,8,14,14,57,25,173000000]}]* Connection #0 to host localhost left intact
```

As you see, the data is initialized and *createdDate* is inserted automatically.



## Data Auditing Support

Spring Data Mongo supports data auditing as Spring Data JPA, it can set the current user and created/last modified timestamp to a field automatically.

Add `EnableMongoAuditing` to application class to activate auditing for MongoDB.

```java
@EnableReactiveMongoAuditing
public class DemoApplication {}
```

In `Post` document, add a new field `createdDate`, annotated it with `@CreatedDate`, it will fill the *createdDate* with current date when inserting it into MongoDB.

```java
@CreatedDate
private LocalDateTime createdDate;
```

To fill the auditor automatically, create a `ReactiveAuditorAware` bean.

```java
@Bean
ReactiveAuditorAware<String> auditorAware() {
    return () -> Mono.just("hantsy");
}
```

Add `@CreatedBy` and `@LastModifiedBy` set the current user that creating and modifying the entity. 

```java
class Post {
    //...
    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
    //...
}        
```

 For the complete codes, check [spring-reactive-sample/boot-data-mongo-auditing](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-mongo-auditing).

## QueryDSL 

Spring Data Mongo provide reactive support in the its QueryDSL extension.

Add the following dependencies in your *pom.xml*.

```xml
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-apt</artifactId>
    <scope>provided</scope>
</dependency>
```

And configure the apt-maven-plugin to generate QueryDSL metadata.

```xml
<plugin>
    <groupId>com.mysema.maven</groupId>
    <artifactId>apt-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <goals>
                <goal>process</goal>
            </goals>
            <configuration>
                <outputDirectory>target/generated-sources/java</outputDirectory>
                <!--<processor>com.querydsl.mongodb.morphia.MorphiaAnnotationProcessor</processor>-->
                    <processor>lombok.launch.AnnotationProcessorHider$AnnotationProcessor,org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor
            </processor>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Modify the `PostRepository` as the following.

```java
interface PostRepository extends ReactiveMongoRepository<Post, String>, 	ReactiveQuerydslPredicateExecutor<Post> {
}
```

An example to use QueryDSL API  in your codes.

```java
this.postRepository.findAll(QPost.post.title.containsIgnoreCase("my"))
```

 For the complete codes, check [spring-reactive-sample/boot-data-mongo-querydsl](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-mongo-querydsl).