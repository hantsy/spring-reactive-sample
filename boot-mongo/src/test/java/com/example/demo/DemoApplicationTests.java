package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Slf4j
class DemoApplicationTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testProductRepository() {
        AtomicReference<String> idReference = new AtomicReference<>();

        productRepository.save(new Product(null, "test", BigDecimal.ONE))
                .as(StepVerifier::create)
                .consumeNextWith(product -> {
                    assertThat(product).isNotNull();
                    assertThat(product.getId()).isNotNull();
                    idReference.set(product.getId());
                })
                .verifyComplete();

        var savedId = idReference.get();
        log.debug("savedId={}", savedId);
        assertThat(savedId).isNotNull();

        productRepository.findById(savedId)
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    log.debug("found product by savedId: {}", p);
                    assertThat(p.getName()).isEqualTo("test");
                })
                .verifyComplete();
    }

    @Test
    public void testNotExistingId() {
        productRepository.findById(ObjectId.get().toHexString())
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

}
