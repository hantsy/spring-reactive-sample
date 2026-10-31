package com.example.demo;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.codec.EnumCodec;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.BiFunction;

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
@Slf4j
public class Application {

    @Value("${server.port:8080}")
    private int port = 8080;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                Application.class)) {
            var posts = context.getBean(PostRepository.class);
            posts.findAll()
                    .subscribe(
                            data -> log.debug("found post:{}", data),
                            throwable -> log.debug("error caught:{}", throwable.getMessage())
                    );
        }
    }
}

@Configuration
class DatabaseConfig {

    @Bean
    public ConnectionFactory connectionFactory(Environment env) {

        // 1. using ConnectionFactories.get from url
        //ConnectionFactory factory = ConnectionFactories.get("r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

        // 2. using r2dbc drivers provided tools to create a connection factory.

        //  H2
        //see: https://github.com/spring-projects/spring-data-r2dbc/issues/269
        //        return new H2ConnectionFactory(
        //                H2ConnectionConfiguration.builder()
        //                        //.inMemory("testdb")
        //                        .file("./testdb")
        //                        .username("user")
        //                        .password("password").build()
        //        );
        //
        //        return H2ConnectionFactory.inMemory("testdb");


        // postgres
        CodecRegistrar codecRegistrar = EnumCodec.builder()
                .withEnum("post_status", Post.Status.class)
                .build();
        String host = env.getProperty("r2dbc.host", "localhost");
        Integer port = env.getProperty("r2dbc.port", Integer.class);
        String dbName = env.getProperty("r2dbc.databaseName", "testdb");
        String user = env.getProperty("r2dbc.username", "user");
        String password = env.getProperty("r2dbc.password", "password");

        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(host)
                        .port(port != null ? port : 3456)
                        .database(dbName)
                        .username(user)
                        .password(password)
                        .codecRegistrar(codecRegistrar)
                        .build()
        );
    }

    @Bean
    TransactionAwareConnectionFactoryProxy transactionAwareConnectionFactoryProxy(ConnectionFactory connectionFactory) {
        return new TransactionAwareConnectionFactoryProxy(connectionFactory);
    }

    @Bean
    DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                //.bindMarkers(() -> BindMarkersFactory.named(":", "", 20).create())
                .namedParameters(true)
                .build();
    }

    @Bean
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

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

}

@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer {

    private final DatabaseClient databaseClient;

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() throws Exception {
        log.info("start data initialization...");
        this.databaseClient
                .sql("INSERT INTO  posts (title, content) VALUES (:title, :content)")
                .filter((statement, _) -> statement.returnGeneratedValues("id").execute())
                .bind("title", "my first post")
                .bind("content", "content of my first post")
                .fetch()
                .first()
                .subscribe(
                        data -> log.info("inserted data : {}", data),
                        error -> log.info("error: {}", error)
                );

    }
}


record Post(
        UUID id,
        String title,
        String content,
        Status status,
        List<String> tags
) {

    public static Post of(String title, String content) {
        return new Post(null, title, content, Status.DRAFT, List.of("default"));
    }

    public static Post of(String title, String content, List<String> tags) {
        return new Post(null, title, content, Status.DRAFT, tags);
    }

    enum Status {
        DRAFT, PENDING_MODERATION, PUBLISHED;
    }

}

@RequiredArgsConstructor
@Component
@Slf4j
class PostRepository {

    public static final BiFunction<Row, RowMetadata, Post> MAPPING_FUNCTION = (row, _) -> {
        String[] tags = row.get("tags", String[].class);
        return new Post(
                row.get("id", UUID.class),
                row.get("title", String.class),
                row.get("content", String.class),
                row.get("status", Post.Status.class),
                tags == null ? Collections.emptyList() : Arrays.asList(tags)
        );
    };

    private final DatabaseClient databaseClient;

    public Flux<Post> findByTitleContains(String name) {
        return this.databaseClient
                .sql("SELECT * FROM posts WHERE title LIKE :title")
                .bind("title", "%" + name + "%")
                .map(MAPPING_FUNCTION)
                .all();
    }

    public Flux<Post> findAll() {
        return this.databaseClient
                .sql("SELECT * FROM posts")
                .filter((statement, executeFunction) -> statement.fetchSize(10).execute())
                .map(MAPPING_FUNCTION)
                .all();
    }

    // see:
    // https://stackoverflow.com/questions/64267699/spring-data-r2dbc-and-group-by
    public Flux<Map<Object, Object>> countByStatus() {
        return this.databaseClient
                .sql("SELECT count(*) as cnt, status FROM posts group by status")
                .map((row, rowMetadata) -> {
                    Long cnt = row.get("cnt", Long.class);
                    Post.Status s = row.get("status", Post.Status.class);

                    return Map.<Object, Object>of("cnt", cnt, "status", s);
                })
                .all();
    }

    public Mono<Post> findById(UUID id) {
        return this.databaseClient
                .sql("SELECT * FROM posts WHERE id=:id")
                .bind("id", id)
                .map(MAPPING_FUNCTION)
                .one();
    }

    public Mono<UUID> save(Post p) {
        return this.databaseClient.sql(
                        "INSERT INTO  posts (title, content, status, tags) VALUES (:title, :content, :status, :tags)")
                .filter((statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
                .bindValues(
                        Map.of("title", p.title(),
                                "content", p.content(),
                                "status", p.status(),
                                "tags", p.tags().toArray(new String[0]))
                )
                .fetch()
                .first()
                .map(r -> (UUID) r.get("id"));
    }

    // see: https://github.com/spring-projects/spring-data-r2dbc/issues/259
    // and
    // https://stackoverflow.com/questions/62514094/how-to-execute-multiple-inserts-in-batch-in-r2dbc
    public Flux<UUID> saveAll(List<Post> data) {
        Assert.notEmpty(data, "saving data can be empty");
        return this.databaseClient.inConnectionMany(connection -> {

            var statement = connection
                    .createStatement("INSERT INTO  posts (title, content, status, tags) VALUES ($1, $2, $3, $4)")
                    .returnGeneratedValues("id");

            for (int i = 0; i < data.size() - 1; i++) {
                Post p = data.get(i);
                statement.bind(0, p.title())
                        .bind(1, p.content())
                        .bind(2, p.status())
                        .bind(3, p.tags().toArray(new String[0]))
                        .add();
            }

            // for the last item, do not call `add`
            var lastItem = data.getLast();
            statement.bind(0, lastItem.title())
                    .bind(1, lastItem.content())
                    .bind(2, lastItem.status())
                    .bind(3, lastItem.tags().toArray(new String[0]));

            return Flux.from(statement.execute())
                    .flatMap(result -> result.map((row, rowMetadata) -> row.get("id", UUID.class)));
        });
    }

    public Mono<Long> update(Post p) {
        return this.databaseClient
                .sql("UPDATE posts set title=:title, content=:content, status=:status, tags=:tags WHERE id=:id")
                .bindValues(
                        Map.of("title", p.title(),
                                "content", p.content(),
                                "status", p.status(),
                                "tags", p.tags().toArray(new String[0]),
                                "id", p.id())
                )
                .fetch()
                .rowsUpdated();
    }

    public Mono<Long> deleteById(UUID id) {
        return this.databaseClient.sql("DELETE FROM posts WHERE id=:id")
                .bind("id", id)
                .fetch()
                .rowsUpdated();
    }

    public Mono<Long> deleteAllById(List<UUID> ids) {
        return this.databaseClient.sql("DELETE FROM posts WHERE id in (:ids)")
                .bind("ids", ids)
                .fetch()
                .rowsUpdated();
    }

    public Mono<Long> deleteAll() {
        return this.databaseClient.sql("DELETE FROM posts")
                .fetch()
                .rowsUpdated();
    }
}