# Customizing queries with Spring Data Neo4j

In [the former post](./data-neo4j.md), we've created a simple project and used the declaration approach to create a `Repository` to access the Neo4j server. Similar to other Spring Data modules, Spring Data Neo4j provides a `ReactiveNeo4jOperations` for reactive applications to interact with Neo4j servers by a programmatic approach, and additionally, Spring Data Neo4j provides a low-level API abstraction to execute the [Cyper Query Language](https://neo4j.com/developer/cypher/) through the `ReactiveNeo4jClient` bean.

## Conventional derived query methods

Like other Spring Data modules, Spring Data Neo4j also supports derived query methods.

For example, to find the posts by keyword that matches the title field, add the following method in the `PostRepository` interface.

```java
interface PostRepository extends ReactiveNeo4jRepository<Post, Long> {

    Flux<Post> findByTitleLike(String title);
}
```

Add a new test method in `PostRespositoryTest` to verify it.

```java 
@Test
void testFindByTitle() {
    posts.findByTitleLike("one")
        .as(StepVerifier::create)
        .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
        .verifyComplete();
}
```

Run the test, it should work as expected.

## Customizing Query by @Query annotation

Like the @Query in Spring Data JPA,  Spring Data Neo4j  provides its own `@Query` annotation to attach the result of a custom Cyper Query to the method. 

```java
import org.springframework.data.neo4j.repository.query.Query;
//...
    
interface PostRepository extends ReactiveNeo4jRepository<Post, Long> {
    @Query("MATCH(post:Post) WHERE post.title =~ $title RETURN post")
    Flux<Post> findByTitleContains(String title);
    
    //
	...
}
```

Here we use a `regex` pattern in the where clause.

> More details about the syntax of  Cyper Query Language, please check the [official Neo4j documentation](https://neo4j.com/developer/cypher/).

Add a test method to verify it.

```java
@Test
void testFindByQuery() {
    posts.findByTitleContains("(?i).*" + "one" + ".*")
        .as(StepVerifier::create)
        .consumeNextWith(p -> assertEquals("Post one", p.getTitle()))
        .verifyComplete();
}
```

Here the `findByTitleContains` method has to accept a Regex pattern.

## ReactiveNeo4jClient

Once Spring Data Neo4j is configured in a reactive application, a `ReactvieNeo4jClient` bean is available in the Spring application context. 

Like the R2dbc's `DatabaseClient` , with `ReactiveNeo4jClient`, you can execute custom Cyper Queries and handle returning result freely. 

For example, to find all posts, it can be done by the following method.

```java
public Flux<Post> findAll() {
    var query = """
        MATCH (p:Post)
        RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
        """;
        return client
        .query(query)
        .fetchAs(Post.class).mappedBy((ts, r) ->
                                      Post.builder()
                                      .id(r.get("id").asLong())
                                      .title(r.get("title").asString())
                                      .content(r.get("content").asString())
                                      .createdAt(r.get("createdAt").asLocalDateTime(null))
                                      .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                      .build()
                                     )
        .all();
}
```

In the above codes. 

* The `query` use a multi-lined text block(available in the latest Java 15) to define a Cyper query.
* The  `client.query` to execute the defined query.
* The `fetchAs` to handle the returning result, similar to RowMapper in Jdbc/R2dbc to extract the result and wrap it into a POJO class.
* The `all` will return a `Flux` , if you want to return a single result, use `one` instead.

The following is an example of  basic CRUD operations.

```java
@Component
@RequiredArgsConstructor
public class PostRepository {

    private final ReactiveNeo4jClient client;

    public Mono<Long> count() {
        var query = """
                MATCH (p:Post) RETURN count(p)
                """;
        return client.query(query)
                .fetchAs(Long.class)
                .mappedBy((ts, r) -> r.get(0).asLong())
                .one();
    }

    public Flux<Post> findAll() {
        var query = """
                MATCH (p:Post)
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .all();
    }

    public Flux<Post> findByTitleContains(String title) {
        var query = """
                MATCH (p:Post)
                WHERE p.title =~ $title
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .bind("(?!).*" + title + ".*").to("title")
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .all();
    }

    public Mono<Post> findById(Long id) {
        var query = """
                MATCH (p:Post)
                WHERE p.id = $id
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;
        return client
                .query(query)
                .bind(id).to("id")
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .one();
    }

    public Mono<Post> save(Post post) {
        var query = """
                MERGE (p:Post {id: $id})
                ON CREATE SET p.createdAt=localdatetime(), p.title=$title, p.content=$content
                ON MATCH SET p.updatedAt=localdatetime(), p.title=$title, p.content=$content
                RETURN p.id as id, p.title as title, p.content as content, p.createdAt as createdAt, p.updatedAt as updatedAt
                """;

        return client.query(query)
                .bind(post).with(data ->
                        Map.of(
                                "id", (data.getId() != null ? data.getId() : UUID.randomUUID().toString()),
                                "title", data.getTitle(),
                                "content", data.getContent()
                        )
                )
                .fetchAs(Post.class).mappedBy((ts, r) ->
                        Post.builder()
                                .id(r.get("id").asLong())
                                .title(r.get("title").asString())
                                .content(r.get("content").asString())
                                .createdAt(r.get("createdAt").asLocalDateTime(null))
                                .updatedAt(r.get("updatedAt").asLocalDateTime(null))
                                .build()
                )
                .one();
    }

    public Mono<Integer> deleteAll() {
        var query = """
                MATCH (m:Post) DETACH DELETE m
                """;
        return client.query(query)
                .run()
                .map(it -> it.counters().nodesDeleted());

    }

    public Mono<Integer> deleteById(Long id) {
        var query = """
                MATCH (p:Post) WHERE p.id = $id
                DETACH DELETE p
                """;
        return client
                .query(query)
                .bind(id).to("id")
                .run()
                .map(it -> it.counters().nodesDeleted());
    }
}

```

> Note: Please navigate to the **data-neo4j** and **boot-neo4j-cyper** repositories to check the above example codes.

## ReactiveNeo4jOperations

Like other Spring Data modules, Spring Data Neo4j provides a `ReactiveNeo4jOperations`(and  the implementation `ReactiveNeo4jTemplate`), it allows your to perform operations on Neo4j databases but by programmatic approaches.

Here is an example of `PostRepository`  which is reimplemented by `ReactiveNeo4jOperations`.

```java
@Component
@RequiredArgsConstructor
public class PostRepository  {
    private final ReactiveNeo4jOperations template;


    public Mono<Long> count() {
        return this.template.count(Post.class);
    }


    public Flux<Post> findAll() {
        return this.template.findAll(Post.class);
    }


    public Mono<Post> findById(Long id) {
        return this.template.findById(id, Post.class);
    }


    public Flux<Post> findByTitleContains(String title) {
        var postNode = node("Post").named("p");
        return this.template.findAll(
                match(postNode)
                        .where(postNode.property("title").contains(literalOf(title)))
                        .returning(postNode)
                        .build(),
                Post.class
        );
    }



    public Mono<Post> save(Post post) {
        return this.template.save(post);
    }


    public Flux<Post> saveAll(List<Post> data) {
        return this.template.saveAll(data);
    }


    public Mono<Void> deleteById(Long id) {
        return this.template.deleteById(id, Post.class);
    }


    public Mono<Void> deleteAll() {
        return this.template.deleteAll(Post.class);
    }
}

```

It it similar to the ReactiveNeo4jClient, but more simple.  Have a look at the `findAll`, the literal queries are replaced by Java Query Criteria APIs.

> Note: Please navigate to the **data-neo4j** and **boot-neo4j** repositories to check the above example codes.

Grab the [source code](http://github.com/hantsy/spring-reactive-sample) from my github.

