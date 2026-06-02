package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Values;
import org.neo4j.driver.reactivestreams.ReactiveResult;
import org.neo4j.driver.reactivestreams.ReactiveSession;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {
    private final Driver driver;

    Mono<Product> save(Product product) {
        String query = """
                MERGE (p:Product {id: $id})
                ON CREATE SET p.name=$name, p.price=$price
                ON MATCH SET p.name=$name, p.price=$price
                RETURN p.id as id, p.name as name, p.price as price
                """;
        Map<String, Object> parameters = Map.of("id", product.id() != null ? product.id() : UUID.randomUUID().toString(),
                "name", product.name(),
                "price", Values.value(product.price().toString())
        );

        SessionConfig sessionConfig = SessionConfig.builder()
                .withBookmarkManager(driver.executableQueryBookmarkManager())
                .build();

        // Open, execute, and automatically close a session for this specific call
        return Mono.usingWhen(
                Mono.fromSupplier(() -> driver.session(ReactiveSession.class, sessionConfig)),
                session -> Mono.from(session.executeWrite(tc -> Mono.from(tc.run(query, parameters))
                        .flatMapMany(ReactiveResult::records)
                        .single()
                        .map(result -> {
                            log.debug("saving product {}", result);
                            return new Product(
                                    result.get("id").asString(),
                                    result.get("name").asString(),
                                    new BigDecimal(result.get("price").asString())
                            );
                        })
                )),
                ReactiveSession::close // Closes the session on success/completion
        );
    }

    Mono<Product> findById(String id) {
        String query = """
                MATCH (p:Product)
                WHERE p.id=$id
                // Return the full properties map instead of property individual paths
                // RETURN p.id as id, p.name as name, p.price as price
                RETURN p { .id, .name, .price } as productData
                """;
        Map<String, Object> parameters = Map.of("id", id);
        SessionConfig sessionConfig = SessionConfig.builder()
                .withBookmarkManager(driver.executableQueryBookmarkManager())
                .build();

        // Open, execute, and automatically close a session for this specific call
        return Mono.usingWhen(
                Mono.fromSupplier(() -> driver.session(ReactiveSession.class, sessionConfig)),
                session -> Mono.from(session.executeRead(tc -> Mono.from(tc.run(query, parameters))
                        .flatMapMany(ReactiveResult::records)
                        .next()
                        .map(result -> {
                            // Extracting using standard .get("productData").get("id") syntax
                            var data = result.get("productData");
                            return new Product(
                                    data.get("id").asString(),
                                    data.get("name").asString(),
                                    new BigDecimal(data.get("price").asString())
                            );
                        })
                )),
                ReactiveSession::close // Closes the session on success/completion
        );
    }
}
