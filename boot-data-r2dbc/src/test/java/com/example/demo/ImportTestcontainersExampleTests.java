package com.example.demo;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class ImportTestcontainersExampleTests {

    @TestConfiguration(proxyBeanMethods = false)
    @ImportTestcontainers(MyContainers.class)
    static class MyTestConfig {
    }

    interface MyContainers {
        @Container
        @ServiceConnection
        PostgreSQLContainer PG_CONTAINER = new PostgreSQLContainer(DockerImageName.parse("postgres:14"));
    }

//    class MyContainers {
//        @Container
//        @ServiceConnection // to establish service connection
//        static PostgreSQLContainer PG_CONTAINER = new PostgreSQLContainer(DockerImageName.parse("postgres:14"));
//    }

//    class MyContainers {
//
//        @Container
//        static PostgreSQLContainer PG_CONTAINER = new PostgreSQLContainer(DockerImageName.parse("postgres:14"));
//
//        @DynamicPropertySource
//        static PostgreSQLContainer postgreSQLContainer(DynamicPropertyRegistry registry) {
//            registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + PG_CONTAINER.getHost() + ":" + PG_CONTAINER.getFirstMappedPort() + "/" + PG_CONTAINER.getDatabaseName());
//            registry.add("spring.r2dbc.username", PG_CONTAINER::getUsername);
//            registry.add("spring.r2dbc.password", PG_CONTAINER::getPassword);
//
//            return PG_CONTAINER;
//        }
//    }

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