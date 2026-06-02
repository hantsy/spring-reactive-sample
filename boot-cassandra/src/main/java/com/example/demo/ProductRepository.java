package com.example.demo;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.oss.driver.api.core.CqlSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {

    private final CqlSession cqlSession;

    Mono<Product> save(Product product) {
        var id = product.id() != null ? product.id() : UUID.randomUUID().toString();

        // IF NOT EXISTS will save `wasApplied` in the rsult
        String query = """
                INSERT INTO products(id, name, price)
                VALUES (:id, :name, :price)
                IF NOT EXISTS
                """;
        ReactiveResultSet resultSet = cqlSession.executeReactive(query, Map.of("id", id,
                "name", product.name(),
                "price", product.price())
        );

        return Mono.from(resultSet)
                .flatMap(row -> {
                    log.info("Inserted product with id {}", id);

                    if (row.wasApplied()) {
                        return Mono.just(new Product(id, product.name(), product.price()));
                    } else {
                        return Mono.empty();
                    }
                });
    }

    Mono<Product> findById(String id) {
        String query = """
                SELECT * FROM products WHERE id = :id
                """;
        ReactiveResultSet resultSet = cqlSession.executeReactive(query, Map.of("id", id));
        return Mono.from(resultSet)
                .flatMap(row -> Mono.just(new Product(
                                        row.get("id", String.class),
                                        row.get("name", String.class),
                                        row.get("price", BigDecimal.class)
                                )
                        )
                );
    }
}
