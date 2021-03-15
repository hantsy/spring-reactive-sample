---
sort: 6
---

# Spring Data Cassandra

Spring Data Cassandra also embraces reactive support.

Firstly add the following dependencies into your project.

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-cassandra</artifactId>
</dependency>
```

Create a `@Configuration` class to configure Cassandra and enable reactive support.

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

`getKeyspaceCreations` configures how to create the keyspace when Cassandra is started, here we create the keyspace if it does not existed.

`getSchemaAction` specifies the action of schema generation.

Next add `Table` annotation to the `Post` entity.

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

Unlike Mongo, in Cassandra, you have to fill the `id` field manually before it is inserted.

Next change the former `PostRepository` to the following:

```java
interface PostRepository extends ReactiveCassandraRepository<Post, String>{}
```

Cassandra has a reactive variant for `Repository`, as the above `CassandraRepository`.

## Spring Boot

If you are using Spring Boot, just add `spring-boot-starter-data-cassandra-reactive` into your project dependencies.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
</dependency>
```

No need extra configuration, Spring Boot will configure Cassandra for you and registers related beans for you.

## Data initialization

As former Mongo example, it is easy to erase the existing data and import some initial data when the application is started up.

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

