package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Slf4j
class DemoApplicationTests {

    @Autowired
    ProductRepository productRepository;

    @Test
    public void testProductRepository() {
        AtomicReference<String> idReference = new AtomicReference<>();

        String insertedId = UUID.randomUUID().toString();
        productRepository.save(new Product(insertedId, "test", BigDecimal.ONE))
                .as(StepVerifier::create)
                .consumeNextWith(product -> {
                    idReference.set(product.id());
                    assertThat(product).isNotNull();
                    assertThat(product.id()).isNotNull();
                })
                .verifyComplete();

        var savedId = idReference.get();
        log.debug("savedId={}", savedId);
        assertThat(savedId).isNotNull();
        assertThat(savedId).isEqualTo(insertedId);

        productRepository.findById(savedId)
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    log.debug("found product by savedId: {}", p);
                    assertThat(p.name()).isEqualTo("test");
                })
                .verifyComplete();
    }

    @Test
    public void testNotExistingId() {
        productRepository.findById(UUID.randomUUID().toString())
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

}
