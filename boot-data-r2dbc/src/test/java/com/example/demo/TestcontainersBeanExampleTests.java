package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class TestcontainersBeanExampleTests {

    @TestConfiguration(proxyBeanMethods = false)
    static class MyTestConfig {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgreSQLContainer() {
            return new PostgreSQLContainer(DockerImageName.parse("postgres:14"));
        }

//        @Bean
//        PostgreSQLContainer postgreSQLContainer(DynamicPropertyRegistry registry) {
//            PostgreSQLContainer PG_CONTAINER = new PostgreSQLContainer(DockerImageName.parse("postgres:14"));
//
//            registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + PG_CONTAINER.getHost() + ":" + PG_CONTAINER.getFirstMappedPort() + "/" + PG_CONTAINER.getDatabaseName());
//            registry.add("spring.r2dbc.username", PG_CONTAINER::getUsername);
//            registry.add("spring.r2dbc.password", PG_CONTAINER::getPassword);
//
//            return PG_CONTAINER;
//        }
    }

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testProductRepository() {
        var product = productRepository.save(new Product(null, "test", BigDecimal.ONE));

        product.as(StepVerifier::create)
                .consumeNextWith(p -> {
                    assertThat(p).isNotNull();
                    assertThat(p.id()).isNotNull();
                })
                .verifyComplete();
    }


}
