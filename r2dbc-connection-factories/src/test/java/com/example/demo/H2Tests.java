package com.example.demo;

import io.r2dbc.spi.Connection;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class H2Tests {

    @Nested
    @Slf4j
    static class Connections {

        @Test
        public void getFromUrl() {
            var conn = H2ConnectionFactories.fromUrl();
            var metadata = conn.getMetadata();
            log.info("ConnectionFactoryMetadata name: {}", metadata.getName());
            assertThat(conn).isNotNull();
        }

        @Test
        public void inMemory() {
            var conn = H2ConnectionFactories.inMemory();

            assertThat(conn).isNotNull();
        }

        @Test
        public void file() {
            var conn = H2ConnectionFactories.file();
            assertThat(conn).isNotNull();
        }
    }

    @Nested
    @Slf4j
    static class Sql {
        Publisher<? extends Connection> conn;

        @BeforeEach
        public void setupAll() {
            conn = H2ConnectionFactories.fromUrl().create();
            String createSql = """
                    CREATE TABLE IF NOT EXISTS persons (
                    id SERIAL PRIMARY KEY,
                    first_name VARCHAR(255),
                    last_name VARCHAR(255),
                    age INTEGER
                    )
                    """;

            String insertSql = """
                    INSERT INTO persons(first_name, last_name, age)
                    VALUES
                    ('Hello', 'Kitty', 20),
                    ('Hantsy', 'Bai', 40)
                    """;
            Mono.from(conn)
                    .flatMap(
                            c -> Mono.from(c.createStatement(createSql)
                                    .execute())
                    )
                    .log()
                    .doOnNext(data -> log.info("created: {}", data))
                    .then()
                    .thenMany(
                            Mono.from(conn)
                                    .flatMapMany(
                                            c -> c.createStatement(insertSql)
                                                    .returnGeneratedValues("id")
                                                    .execute()

                                    )
                    )
                    .flatMap(data -> Flux.from(data.map((row, rowMetadata) -> row.get("id"))))
                    .doOnNext(id -> log.info("generated id: {}", id))
                    .blockLast(Duration.ofSeconds(5));
        }

        @AfterEach
        public void teardown() {
            String deleteSql = """
                    DELETE FROM persons
                    """;
            Mono.from(conn)
                    .flatMap(
                            c -> Mono.from(c.createStatement(deleteSql).execute())
                    )
                    .log()
                    .doOnNext(data -> log.info("deleted: {}", data))
                    .subscribe();
        }

        @Test
        public void testQueries() {
            var selectSql = """
                    SELECT * FROM persons;
                    """;
            Mono.from(conn)
                    .flatMapMany(
                            c -> Flux.from(c.createStatement(selectSql)
                                    .execute())
                    )
                    .log()
                    .flatMap(result -> result
                            .map((row, rowMetadata) -> {
                                rowMetadata.getColumnMetadatas()
                                        .forEach(
                                                columnMetadata -> log.info("column name:{}, type: {}", columnMetadata.getName(), columnMetadata.getJavaType())
                                        );
                                var id = row.get("id", Integer.class);
                                var firstName = row.get("first_name", String.class);
                                var lastName = row.get("last_name", String.class);
                                var age = row.get("age", Integer.class);

                                return Person.builder().firstName(firstName)
                                        .lastName(lastName)
                                        .age(age)
                                        .id(id)
                                        .build();
                            }))
                    .doOnNext(data -> log.info(": {}", data))
                    .as(StepVerifier::create)
                    .consumeNextWith(s -> assertThat(s.getFirstName()).isEqualTo("Hello"))
                    .consumeNextWith(s -> assertThat(s.getFirstName()).isEqualTo("Hantsy"))
                    .verifyComplete();
        }


        @Test
        public void testQueryByParam() {
            var selectSql = """
                    SELECT * FROM persons WHERE first_name=$1
                    """;
            Mono.from(conn)
                    .flatMapMany(
                            c -> Flux.from(c.createStatement(selectSql)
                                    .bind("$1", "Hantsy")
                                    .execute())
                    )
                    .log()
                    .flatMap(result -> result
                            .map((row, rowMetadata) -> row.get("first_name", String.class)))
                    .doOnNext(data -> log.info(": {}", data))
                    .as(StepVerifier::create)
                    .consumeNextWith(s -> assertThat(s).isEqualTo("Hantsy"))
                    .verifyComplete();
        }

        @Test
        public void testUpdate() {
            var updateSql = """
                    UPDATE persons SET first_name=$1 WHERE first_name=$2
                    """;
            Mono.from(conn)
                    .flatMapMany(
                            c -> Flux.from(c.createStatement(updateSql)
                                    .bind("$1", "test1")
                                    .bind("$2", "Hantsy")
                                    .execute())
                                    .flatMap(result -> result.getRowsUpdated())
                                    .doOnNext(data -> log.info(": {}", data))
                    )
                    .log()
                    .as(StepVerifier::create)
                    .consumeNextWith(s -> assertThat(s).isEqualTo(1))
                    .verifyComplete();
        }

        @Test
        public void testTransaction() {
            var selectAllSql = """
                    SELECT * FROM persons
                    """;
            var selectSql = """
                    SELECT * FROM persons WHERE first_name=$1
                    """;

            var selectByIdSql = """
                    SELECT * FROM persons WHERE id=$1
                    """;

            var updateSql = """
                    UPDATE persons SET first_name=$1 WHERE id=$2
                    """;

            Mono.from(conn)
                    .flatMapMany(
                            c -> c.createStatement(selectSql)
                                    .bind("$1", "Hantsy")
                                    .execute()
                    )
                    .log()
                    .flatMap(result -> result.map((row, rowMetadata) -> row.get("id", Integer.class)))
                    .flatMap(id ->
                            Mono.from(conn)
                                    .flatMapMany(c ->
                                            Flux.from(c.setAutoCommit(false))
                                                    .flatMap(it -> c.beginTransaction())
                                                    .flatMap(it -> c.createStatement(updateSql)
                                                            .bind("$1", "test1")
                                                            .bind("$2", id)
                                                            .execute()
                                                    )
                                                    .flatMap(it -> c.createSavepoint("save"))
                                                    .flatMap(it -> c.createStatement(updateSql)
                                                            .bind("$1", "test100")
                                                            .bind("$2", id)
                                                            .execute()
                                                    )
                                                    //.flatMap(it -> c.createSavepoint("save2"))
                                                    .flatMap(it -> c.rollbackTransactionToSavepoint("save"))
                                                    .flatMap(it -> c.commitTransaction())
                                                    //.flatMap(it -> c.releaseSavepoint("save"))
                                                    .then()
                                                    .thenMany(
                                                            c.createStatement(selectByIdSql)
                                                                    .bind("$1", id)
                                                                    .execute()
                                                    )
                                                    .log()
                                                    .flatMap(result -> result
                                                            .map((row, rowMetadata) -> row.get("first_name", String.class)))
                                                    .doOnNext(data -> log.info(": {}", data))


                                    )

                    )
                    .as(StepVerifier::create)
                    .consumeNextWith(s -> assertThat(s).isEqualTo("test1"))
                    .verifyComplete();
        }

    }


    @Nested
    @Slf4j
    static class Batch {
        Publisher<? extends Connection> conn;

        @BeforeEach
        public void setupAll() {
            conn = H2ConnectionFactories.fromUrl().create();
            String createSql = """
                    CREATE TABLE IF NOT EXISTS books (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255),
                    author VARCHAR(255)
                    )
                    """;


            Mono.from(conn)
                    .flatMap(
                            c -> Mono.from(c.createStatement(createSql)
                                    .execute())
                    )
                    .log()
                    .doOnNext(data -> log.info("created: {}", data))
                    .block(Duration.ofSeconds(5));
        }


        @Test
        public void testBatch() {
            String insertSql = """
                    INSERT INTO books(name, author)
                    VALUES
                    ('Pro Spring', 'unknown'),
                    ('Spring in action', 'test')
                    """;

            String selectSql = """
                    select * from books
                    """;

            Mono.from(conn)
                    .flatMapMany(
                            c -> {
                                var batch = c.createBatch();
                                return Flux.from(batch.add(insertSql).add(selectSql).execute());
                            }
                    )
                    .doOnNext(r -> log.info("batch result: {}", r))
                    .as(StepVerifier::create)
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @AfterEach
        public void teardown() {
            String deleteSql = """
                    DELETE FROM books
                    """;
            Mono.from(conn)
                    .flatMap(
                            c -> Mono.from(c.createStatement(deleteSql).execute())
                    )
                    .log()
                    .doOnNext(data -> log.info("deleted: {}", data))
                    .subscribe();
        }
    }
}
