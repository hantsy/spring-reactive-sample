package com.example.demo;

import co.elastic.clients.elasticsearch.core.GetResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {

    // Elasticsearch does not provide a reactive client?
    private final ReactiveElasticsearchClient client;

    @SneakyThrows
    Mono<Product> save(Product product) {
        var id = product.id() != null ? product.id() : UUID.randomUUID().toString();
        log.debug("Saving product with id={}", id);
        return client.index(builder -> builder.id(id).index("products").document(product))
                .mapNotNull(response -> {
                    var savedID = response.id();
                    log.debug("Saved product with id={}", savedID);
                    return new Product(savedID, product.name(), product.price());
                });
    }

    @SneakyThrows
    Mono<Product> findById(String id) {
        return this.client.get(builder -> builder.id(id).index("products"), Product.class)
                .flatMap(response -> {
                    if (response.found()) {
                        return Mono.just(response.source());
                    } else {
                        return Mono.empty();
                    }
                });
    }
}
