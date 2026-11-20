
# Mongo-backed sessions

> **Deprecated.** Spring Session 4.0 removed its MongoDB module, so `spring-session-data-mongodb` is no longer part of the Spring Session BOM and has no 4.x release — see [spring-boot#47662](https://github.com/spring-projects/spring-boot/issues/47662) ("Remove support for Spring Session Data MongoDB", closed in commit `c477d3b`). The last release from the Spring Session project is `3.5.7`, which is what the legacy sample pins. The maintained replacement is [`org.mongodb:mongodb-spring-session`](https://github.com/mongodb/mongo-spring-session), published by the MongoDB team for Spring Session 4.0+, with the classes relocated to `org.mongodb.spring.session.*`.

The [spring-session-data-mongodb](https://github.com/spring-projects/spring-session) module stores sessions in MongoDB through the reactive Mongo driver — a natural choice when your application already uses [Spring Data Mongo](../../data/mongo.md).

## Step 1: add the dependencies

```xml
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-mongodb</artifactId>
    <version>3.5.7</version>
</dependency>
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-reactivestreams</artifactId>
</dependency>
```

## Step 2: configure the Mongo connection

```java
@Configuration
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${mongo.uri}")
    String mongoUri;

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        return "blog";
    }
}
```

With the connection property set in `application.properties`:

```properties
server.port=8080
mongo.uri=mongodb://localhost:27017/
```

Start a MongoDB server for local development:

```bash
docker-compose up mongodb
```

## Step 3: enable Mongo web session

```java
@Configuration
@EnableMongoWebSession
public class SessionConfig {
}
```

`@EnableMongoWebSession` registers a reactive session repository backed by MongoDB automatically. The controller and template code is identical to the [in-memory example](in-memory.md#step-3-use-websession-in-a-controller).

## Verify

Start the application, submit the attribute form at http://localhost:8080, and inspect the `blog` database — the sessions live in a dedicated collection with the attributes and a TTL index for expiry.

You can explore the complete codes in [spring-reactive-sample/legacy/session-data-mongo](https://github.com/hantsy/spring-reactive-sample/tree/master/legacy/session-data-mongo).
