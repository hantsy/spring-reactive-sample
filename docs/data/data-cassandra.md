---
sort: 6
---

# Spring Data Cassandra

Spring Data Cassandra adds basic Reactive support.


Follow the the [Getting Started](./start) part to create a freestyle(none Spring Boot) or Spring Boot based project skeleton.

If you are not using Spring Boot, firstly add the following dependencies into your project.

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-cassandra</artifactId>
</dependency>
```

Create a `@Configuration` class to configure Cassandra and add `@EnableReactiveCassandraRepositories` to enable reactive `Repository` support.

```java
@Configuration
@EnableReactiveCassandraRepositories(basePackageClasses = {CassandraConfig.class})
public class CassandraConfig extends AbstractReactiveCassandraConfiguration {

    @Value("${cassandra.keyspace-name}")
    String keySpace;

    @Value("${cassandra.contact-points}")
    String contactPoints;

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {

        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(keySpace)
            .ifNotExists()
            .with(KeyspaceOption.DURABLE_WRITES, true);
        //.withNetworkReplication(DataCenterReplication.dcr("foo", 1), DataCenterReplication.dcr("bar", 2));

        return Arrays.asList(specification);
    }

    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return Arrays.asList(DropKeyspaceSpecification.dropKeyspace(keySpace));
    }

    @Override
    protected String getKeyspaceName() {
        return keySpace;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.RECREATE;
    }

}
```

The `getKeyspaceCreations` configures how to create the keyspace when Cassandra is started, here we create the keyspace if it does not existed.

The `getSchemaAction` specifies the action of schema generation.

Create a `Post` class to present the table in Cassandra, and add add a `@Table` annotation on this class.

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
class Post {

    @PrimaryKey()
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String title;
    private String content;

}
```

Add `@PrimaryKey` on `id` field, it indicates `id` is the primary key of `posts` table. 

> Unlike Mongo and other NoSQL, in Cassandra, you have to fill the `id` field manually before it is inserted.

Next change the former `PostRepository` to the following:

```java
interface PostRepository extends ReactiveCassandraRepository<Post, String>{}
```

For the complete codes, check [spring-reactive-sample/data-cassandra](https://github.com/hantsy/spring-reactive-sample/blob/master/data-cassandra).

Alternatively, for Spring Boot applications, just need to add `spring-boot-starter-data-cassandra-reactive` into your project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
</dependency>
```

No need extra configuration, Spring Boot will autoconfigure Cassandra and registers all essential beans for you.

For the complete codes, check [spring-reactive-sample/boot-data-cassandra](https://github.com/hantsy/spring-reactive-sample/blob/master/boot-data-cassandra).

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

