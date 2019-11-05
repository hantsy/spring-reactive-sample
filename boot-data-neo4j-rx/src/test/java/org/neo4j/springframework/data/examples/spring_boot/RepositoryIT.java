/*
 * Copyright (c) 2019 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.springframework.data.examples.spring_boot;

import static org.assertj.core.api.Assertions.*;

import reactor.test.StepVerifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.springframework.boot.test.autoconfigure.data.DataNeo4jTest;
import org.neo4j.springframework.data.examples.spring_boot.domain.MovieRepository;
import org.neo4j.springframework.data.examples.spring_boot.domain.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
@Testcontainers
@EnabledIfEnvironmentVariable(named = RepositoryIT.SYS_PROPERTY_NEO4J_VERSION, matches = "4\\.0.*")
@DataNeo4jTest
@ContextConfiguration(initializers = RepositoryIT.Initializer.class)
class RepositoryIT {

    private static final String SYS_PROPERTY_NEO4J_ACCEPT_COMMERCIAL_EDITION = "SDN_RX_NEO4J_ACCEPT_COMMERCIAL_EDITION";
    protected static final String SYS_PROPERTY_NEO4J_VERSION = "SDN_RX_NEO4J_VERSION";

    @Container
    private static Neo4jContainer<?> neo4jContainer =
            new Neo4jContainer<>("neo4j:" + System.getenv(SYS_PROPERTY_NEO4J_VERSION))
                    .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT",
                            Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_ACCEPT_COMMERCIAL_EDITION)).orElse("no"));

    @Autowired
    private PersonRepository repository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private Driver driver;

    @BeforeEach
    void setup() throws IOException {
        try (BufferedReader moviesReader = new BufferedReader(
                new InputStreamReader(this.getClass().getResourceAsStream("/movies.cypher")));
             Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
            String moviesCypher = moviesReader.lines().collect(Collectors.joining(" "));
            session.run(moviesCypher);
        }
    }

    @Test
    void loadAllPersonsFromGraph() {
        int expectedPersonCount = 133;
        StepVerifier.create(repository.findAll())
                .expectNextCount(expectedPersonCount)
                .verifyComplete();
    }

    @Test
    void findPersonByName() {
        StepVerifier.create(repository.findByName("Tom Hanks"))
                .assertNext(personEntity -> {
                    assertThat(personEntity.getBorn()).isEqualTo(1956);
                })
                .verifyComplete();
    }

    @Test
    void findsPersonsWhoActAndDirect() {
        int expectedActorAndDirectorCount = 5;
        StepVerifier.create(repository.getPersonsWhoActAndDirect())
                .expectNextCount(expectedActorAndDirectorCount)
                .verifyComplete();
    }

    @Test
    void findOneMovie() {
        StepVerifier.create(movieRepository.findOneByTitle("The Matrix"))
                .assertNext(movie -> {
                    assertThat(movie.getTitle()).isEqualTo("The Matrix");
                    assertThat(movie.getDescription()).isEqualTo("Welcome to the Real World");
                    assertThat(movie.getDirectors()).hasSize(2);
                    assertThat(movie.getActors()).hasSize(5);
                })
                .verifyComplete();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "org.neo4j.driver.uri=" + neo4jContainer.getBoltUrl(),
                    "org.neo4j.driver.authentication.username=neo4j",
                    "org.neo4j.driver.authentication.password=" + neo4jContainer.getAdminPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}