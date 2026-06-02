package com.example.demo;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {

    private final MongoClient mongoClient;
    private MongoCollection<Product> productsCollection;

    @PostConstruct
    public void init() {
        this.productsCollection = mongoClient
                .getDatabase("test")
                .getCollection("products", Product.class);
    }

    Mono<Product> save(Product product) {
        return Mono.from(this.productsCollection.insertOne(product))
                .mapNotNull(result -> {
                    log.debug("save product result: {}", result);
                    var id = result.getInsertedId().asObjectId().getValue().toHexString();
                    return product.withId(id);
                });


    }

    Mono<Product> findById(String id) {
        return Flux.from(this.productsCollection.find(Filters.eq(new ObjectId(id))))
                .next()
                .mapNotNull(byId -> {
                    log.debug("find product by id: {}", byId);
                    return byId;
                });
    }

}
