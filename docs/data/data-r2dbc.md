---
sort: 3
---

# Spring Data R2dbc 

> This post focused on Spring Boot 2.3  or previous version which shipped with Spring Data R2dbc 1.1.  

**Spring Data R2dbc 1.2 brought plenty of changes and broken APIs. For the newest Spring Boot 2.4 and Spring Data R2dbc 1.2, please go to [Spring R2dbc Sample](https://github.com/hantsy/spring-r2dbc-sample) and update yourself to the latest R2dbc**.



## Getting Started


Generate a Spring Boot project using [Spring Initializr](https://start.spring.io), and select the following options.

* Project type: Maven
* Java version : 11
* Spring Boot : 2.2.4.RELEASE
* Dependencies: Reactive Web, Lombok, R2dbc 

Open the *pom.xml* file in the project root folder.

There is a `spring-boot-bom-r2dbc` to manage the r2dbc related dependencies. 

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot.experimental</groupId>
            <artifactId>spring-boot-bom-r2dbc</artifactId>
            <version>0.1.0.M3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

A Spring Boot starter `spring-boot-starter-data-r2dbc` dependency is added in the `dependencies`.

```xml
<dependency>
    <groupId>org.springframework.boot.experimental</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
```
Let's add a r2dbc driver for the specific database you are using. 

Use H2 as an example, add the following dependency. Other drivers can found in  [R2dbc website](https://r2dbc.io/) .

```xml
<dependency>
    <groupId>io.r2dbc</groupId>
    <artifactId>r2dbc-h2</artifactId>
    <scope>runtime</scope>
</dependency>
```
In a Spring Boot based project, the `spring-boot-starter-data-r2dbc`  starter will auto-configure all necessary facilities to get the r2dbc work.

In a none Spring Boot project,  you can configure R2dbc manually by declaring a custom `AbstractR2dbcConfiguration`.  

```java

@Configuration
@EnableR2dbcRepositories
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        //ConnectionFactory factory = ConnectionFactories.get("r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

        //see: https://github.com/spring-projects/spring-data-r2dbc/issues/269
//        return new H2ConnectionFactory(
//                H2ConnectionConfiguration.builder()
//                        //.inMemory("testdb")
//                        .file("./testdb")
//                        .username("user")
//                        .password("password").build()
//        );

        return H2ConnectionFactory.inMemory("testdb");
    }

    @Bean
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}   
```
In the above codes:

* `ConnectionFactory` is required to connect to a database.
* and `ReactiveTransactionManager` is use for reactive transaction management.
* `@EnableR2dbcRepositories`  will recognize `Repository` interface.
* In the background, it also configured a `DatabaseClient` which allow you to execute criteria like query on database.


In a  Spring Boot project, all these are configured for you, if you want to adjust some properties, for example, to use PostgresSQL instead of H2, firstly declare a `r2dbc-postgresql` dependency instead.

```xml
<dependency>
    <groupId>io.r2dbc</groupId>
    <artifactId>r2dbc-postgresql</artifactId>
</dependency>
```

Secondly, add r2dbc connection info for your PostgresSQL in *application.properties*.

```properties
spring.r2dbc.url=r2dbc:postgresql://localhost/test
spring.r2dbc.username=user
spring.r2dbc.password=password
spring.r2dbc.initialization-mode=always
```

Let's go ahead.

Create a POJO  to present data in the database table.

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
class Post {
    
    @Id
    @Column("id")
    private Integer id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;
    
}
```

The `@Table` and `@Column` annotations define the mapping rule between the POJO and the backing table and its columns, `@Id`  indicates `id` is the table primary key.

Create a `Repository` interface for the `Post` entity.

```java
interface PostRepository extends ReactiveCrudRepository<Post, Integer> {
    
    @Query("SELECT * FROM posts WHERE title like $1")
    Flux<Post> findByTitleContains(String name);
}
```

As you see, similar to the `Repository` working in other Spring Data projects, it also supports custom `@Query`  on method. But there is a limitation in the current 1.0.0 version of Spring Data R2dbc , the query derivation is not support, if you want to customize the query, the `@Query` is a must.

Next, let's create a `@RestController` to expose simple CRUD endpoints for Post.

```java
@RestController()
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
class PostController {

    private final PostRepository posts;

    @GetMapping("")
    public Flux<Post> all() {
        return this.posts.findAll();
    }

    @PostMapping("")
    public Mono<Post> create(@RequestBody Post post) {
        return this.posts.save(post);
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") Integer id) {
        return this.posts.findById(id);
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") Integer id, @RequestBody Post post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());

                    return p;
                })
                .flatMap(p -> this.posts.save(p));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") Integer id) {
        return this.posts.deleteById(id);
    }

}
```

Unlike JPA, Spring Data R2dbc does not maintain the database schemas, so you have to do it yourself. Spring Data R2dbc provides a `ConnectionFactoryInitializer` to allow you execute sql scripts on database when it is connected. In a Spring Boot application, it is configured for you automatically. When the application is starting up, it will scan `schema.sql` and `data.sql` files in the classpath to initialize the database.

Here is a  `schema.sql` file to create tables.

 ```sql
CREATE TABLE IF NOT EXISTS posts (id SERIAL PRIMARY KEY, title VARCHAR(255), content VARCHAR(255));
 ```

The following script is to load sample data.

```sql
DELETE FROM posts;
INSERT INTO  posts (title, content) VALUES ('post one', 'content of post one');
```

You can also customize a `ConnectionFactoryInitializer` bean to execute the your scripts.

```java
@Bean
public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);

    CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
    populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
    populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("data.sql")));
    initializer.setDatabasePopulator(populator);

    return initializer;
}
```

Additionally, if you are using Docker to serve a database, it also leaves room for you to initialize database when database is started, use volumes to map your local folder which includes a `init.sql` to the path `/docker-entrypoint-initdb.d` in Docker container.

An example for MySQL.

```yaml
  mysql:
    image: mysql:5.7
    ports:
      - "3306:3306"
 #   command: --default-authentication-plugin=mysql_native_password  
    environment:
      MYSQL_ROOT_PASSWORD: mysecret
      MYSQL_USER: user
      MYSQL_PASSWORD: password
      MYSQL_DATABASE: test
    volumes:
 #     - ./data/mysql:/var/lib/mysql    
      - ./mysql-initdb.d:/docker-entrypoint-initdb.d
```

And the following is the content of *mysql-initdb.d/init.sql*.

```sql
use test;

CREATE TABLE IF NOT EXISTS posts(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content VARCHAR(255) NOT NULL
);
```

 If the tables are existed, you can use a `CommandLineRunner` or `ApplicationRunner` to initialize the sample instead of the `data.sql`script. 

```java
@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer implements ApplicationRunner {

    private final PostRepository posts;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("start data initialization...");
        this.posts
                .saveAll(
                        List.of(
                                Post.builder().title("Post one").content("The content of post one").build(),
                                Post.builder().title("Post tow").content("The content of post tow").build()
                        )
                )
                .thenMany(
                        this.posts.findAll()
                )
                .subscribe((data) -> log.info("post:" + data),
                        (err) -> log.error("error" + err),
                        () -> log.info("initialization is done...")
                );
    }
}
```

Now start up your application, and use `curl` to test `/posts` endpoints.

```bash
#curl http://localhost:8080/posts
[{"id":1,"title":"post one","content":"content of post one"},{"id":2,"title":"Post one","content":"The content of post one"},{"id":3,"title":"Post tow","content":"The content of post tow"}]
```

Awesome! it works well.

OK, let's move forward.



## Testing

Spring Data R2dbc also provide a  `@DataR2dbcTest` for test slice purpose which is use for testing your R2dbc related facilities without load all configurations.

```java
@DataR2dbcTest
public class PostRepositoryTest {

    @Autowired
    DatabaseClient client;

    @Autowired
    PostRepository posts;

    @Test
    public void testDatabaseClientExisted() {
        assertNotNull(client);
    }

    @Test
    public void testPostRepositoryExisted() {
        assertNotNull(posts);
    }

    @Test
    public void testInsertAndQuery() {
        this.client.insert()
                .into("posts")
                //.nullValue("id", Integer.class)
                .value("title", "testtitle")
                .value("content", "testcontent")
                .then().block(Duration.ofSeconds(5));

        this.posts.findByTitleContains("testtitle")
                .take(1)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertEquals("testtitle", p.getTitle()))
                .verifyComplete();

    }
}
```

But unfortunately, it does not work as `@DataJpaTest` which can pick up an embedded H2 for test purpose, with `@DataR2dbcTest` , we have to use a runtime database, see [#issue62](https://github.com/spring-projects-experimental/spring-boot-r2dbc/issues/68).


## DatabaseClient

As mentioned,  a `DatabaseClient` is available for database operations in a programmatic approach instead of `Repository` interface declaring.

Here is an example of `PostRepository` using `DatabaseClient`.

```java
@RequiredArgsConstructor
@Component
public class PostRepository {

    private final DatabaseClient databaseClient;

    Flux<Post> findByTitleContains(String name) {
        return this.databaseClient.select()
                .from(Post.class)
                .matching(where("title").like(name))
                .fetch()
                .all();
    }

    public Flux<Post> findAll() {
        return this.databaseClient.select()
                .from(Post.class)
                .fetch()
                .all();
    }

    public Mono<Post> findById(Integer id) {
        return this.databaseClient.select()
                .from(Post.class)
                .matching(where("id").is(id))
                .fetch()
                .one();
    }

    public Mono<Integer> save(Post p) {
        return this.databaseClient.insert().into(Post.class)
                .using(p)
                .fetch()
                .one()
                .map(m -> (Integer) m.get("id"));
    }

    public Mono<Integer> update(Post p) {
        return this.databaseClient.update()
                .table(Post.class)
                .using(p)
                .fetch()
                .rowsUpdated();
    }

    public Mono<Integer> deleteById(Integer id) {
        return this.databaseClient.delete().from(Post.class)
                .matching(where("id").is(id))
                .fetch()
                .rowsUpdated();
    }
}
```

The complete codes can be found in my [Github](https://github.com/hantsy/spring-reactive-sample).

* [docker-compose.yaml](https://github.com/hantsy/spring-reactive-sample/blob/master/docker-compose.yml) serves MySQL, MS SQL, PostgreSQL servers.
* [data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/data-r2dbc) A none Spring Boot sample using H2
* [data-r2dbc-postgresql](https://github.com/hantsy/spring-reactive-sample/tree/master/data-r2dbc-postgresql) A none Spring Boot sample using PostgreSQL
* [boot-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-r2dbc) A Spring Boot based sample using H2 and `DatabaseClient`.
* [boot-data-r2dbc](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-r2dbc) A Spring Boot based sample using H2 and `Repository` interface.
* [boot-data-r2dbc-mysql](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-r2dbc-mysql) A Spring Boot based sample using MySQL and `Repository` interface.
* [boot-data-r2dbc-mssql](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-r2dbc-mssql) A Spring Boot based sample using MSSQL and `Repository` interface.
* [boot-data-r2dbc-postgresql](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-r2dbc-postgresql) A Spring Boot based sample using  PostgresSQL and `Repository` interface.