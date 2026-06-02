package com.example.demo;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ReactiveCollection;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {
    private final Cluster cluster;

    private ReactiveCollection productCollection;

    @PostConstruct
    public void init() {
        this.productCollection = this.cluster
                .bucket("demo")
                .defaultCollection()
                .reactive(); // or collections().createCollection()
    }

    Mono<Product> save(Product product) {
        String id = product.id() != null ? product.id() : UUID.randomUUID().toString();
        return this.productCollection.upsert(id, product)
                .mapNotNull(
                        result -> {
                            log.debug("saving product result: {}", result);
                            return new Product(id, product.name(), product.price());
                        }
                );
    }

    Mono<Product> findById(String id) {

        return this.productCollection.get(id)
                .mapNotNull(result -> {
                    log.debug("finding product result: {}", result);
                    return result.contentAs(Product.class);
                })
                // if  DocumentNotFound exception is caught, return a Mono.emtpy instead.
                .onErrorResume(DocumentNotFoundException.class, e -> Mono.empty());
    }
}
