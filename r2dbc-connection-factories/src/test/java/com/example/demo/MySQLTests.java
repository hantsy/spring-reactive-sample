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

public class MySQLTests {
    @Nested
    @Slf4j
    static class Connections {

        @Test
        public void getFromUrl() {
            var conn = MySQLConnectionFactories.fromUrl();
            var metadata = conn.getMetadata();
            log.info("ConnectionFactoryMetadata name: {}", metadata.getName());
            assertThat(conn).isNotNull();
        }

        @Test
        public void fromOptions() {
            var conn = MySQLConnectionFactories.fromOptions();
            assertThat(conn).isNotNull();
        }

        @Test
        public void pgConnectionFactory() {
            var conn = MySQLConnectionFactories.mysqlConnectionFactory();
            assertThat(conn).isNotNull();
        }
    }

    @Nested
    @Slf4j
    static class Sql {
        Publisher<? extends Connection> conn;

        @BeforeEach
        public void setupAll() {
            conn = MySQLConnectionFactories.mysqlConnectionFactory().create();
            String createSql = """
                    CREATE TABLE IF NOT EXISTS persons (
                    id int auto_increment PRIMARY KEY,
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
                    .doOnNext(id -> log.info("[BeforeEach]generated id: {}", id))
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
        public void testInserts() {
            String insertSql = """
                    INSERT INTO persons(first_name, last_name, age)
                    VALUES('test first name','last name', 24)
                    """;
            Mono.from(conn)
                    .flatMapMany(
                            c -> Flux.from(c.createStatement(insertSql).returnGeneratedValues("id").execute())
                    )
                    .flatMap(data -> Flux.from(data.map((row, rowMetadata) -> row.get("id"))))
                    .doOnNext(id -> log.info("generated id: {}", id))
                    .as(StepVerifier::create)
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        public void testInsertBatch() {
            String insertSql = """
                    INSERT INTO persons(first_name, last_name, age)
                    VALUES(?, ?, ?)
                    """;
            Mono.from(conn)
                    .flatMapMany(
                            c -> {
                                var statement = c.createStatement(insertSql);
                                statement.bind(0, "testfirstname1").bind(1, "testlastname1").bind(2, 10).add();
                                statement.bind(0, "testfirstname2").bind(1, "testlastname2").bind(2, 20).add();
                                statement.bind(0, "testfirstname3").bind(1, "testlastname3").bind(2, 30).add();
                                return Flux.from(statement.returnGeneratedValues("id").execute());
                            }
                    )
                    .flatMap(data -> Flux.from(data.map((row, rowMetadata) -> row.get("id"))))
                    .doOnNext(id -> log.info("generated id: {}", id))
                    .as(StepVerifier::create)
                    .expectNextCount(3)
                    .verifyComplete();
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
                    SELECT * FROM persons WHERE first_name=?
                    """;
            Mono.from(conn)
                    .flatMapMany(
                            c -> Flux.from(c.createStatement(selectSql)
                                    .bind(0, "Hantsy")
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
                    UPDATE persons SET first_name=? WHERE first_name=?
                    """;
            Mono.from(conn)
                    .flatMapMany(
                            c -> Flux.from(c.createStatement(updateSql)
                                    .bind(0, "test1")
                                    .bind(1, "Hantsy")
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
                    SELECT * FROM persons WHERE first_name=?
                    """;

            var selectByIdSql = """
                    SELECT * FROM persons WHERE id=?
                    """;

            var updateSql = """
                    UPDATE persons SET first_name=? WHERE id=?
                    """;

            Mono.from(conn)
                    .flatMapMany(
                            c -> c.createStatement(selectSql)
                                    .bind(0, "Hantsy")
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
                                                            .bind(0, "test1")
                                                            .bind(1, id)
                                                            .execute()
                                                    )
                                                    .flatMap(it -> c.createSavepoint("save"))
                                                    .flatMap(it -> c.createStatement(updateSql)
                                                            .bind(0, "test100")
                                                            .bind(1, id)
                                                            .execute()
                                                    )
                                                    //.flatMap(it -> c.createSavepoint("save2"))
                                                    .flatMap(it -> c.rollbackTransactionToSavepoint("save"))
                                                    .flatMap(it -> c.commitTransaction())
                                                    //.flatMap(it -> c.releaseSavepoint("save"))
                                                    .then()
                                                    .thenMany(
                                                            c.createStatement(selectByIdSql)
                                                                    .bind(0, id)
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

}


