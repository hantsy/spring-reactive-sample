package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
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

	@SneakyThrows
    @Test
	public void testProductRepository() {
		AtomicReference<String> idReference = new AtomicReference<>();

		// First, completely execute and complete the save transaction
		productRepository.save(new Product(null, "test", BigDecimal.ONE))
				.as(StepVerifier::create)
				.consumeNextWith(product -> {
					assertThat(product).isNotNull();
					assertThat(product.id()).isNotNull();
					idReference.set(product.id());
				})
				.verifyComplete(); // The driver captures the write bookmark here

		var savedId = idReference.get();
		log.debug("savedId={}", savedId);
		assertThat(savedId).isNotNull();

		Thread.sleep(Duration.ofMillis(1_000));

		// The read session automatically uses that bookmark to guarantee casual consistency
		productRepository.findById(savedId)
				.delayElement(Duration.ofMillis(500))
				.as(StepVerifier::create)
				.consumeNextWith(p -> {
					log.debug("found product by savedId: {}", p);
					assertThat(p.name()).isEqualTo("test");
				})
				.verifyComplete(); // Suc
	}

	@Test
	public void testNotExistingId() {
		productRepository.findById(UUID.randomUUID().toString())
				.as(StepVerifier::create)
				.expectNextCount(0)
				.verifyComplete();
	}

}
