package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(ContainersConfig.class)
@Slf4j
@ActiveProfiles("test")
class RepositoryTests {

    @Autowired
    MessageRepository messages;

    @Autowired
    ReactiveMongoTemplate template;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        CountDownLatch latch = new CountDownLatch(1);
        template.insert(Message.builder().body("Welcome!").build()).then().block();
        template.executeCommand("{\"convertToCapped\": \"messages\", size: 10000}")
                .log()
                .doOnTerminate(latch::countDown)
                .subscribe(r -> log.debug("executed command: {}", r));

        latch.await();
    }

    @Test
    void testGetAllMessages() throws InterruptedException {
        var verifier = this.messages.getMessagesBy()
                .as(StepVerifier::create)
                .consumeNextWith(m -> assertThat(m.getBody()).isEqualTo("Welcome!"))
                .consumeNextWith(m -> assertThat(m.getBody()).isEqualTo("message one"))
                .consumeNextWith(m -> assertThat(m.getBody()).isEqualTo("message two"))
                .thenCancel()
                .verifyLater();

        this.messages.saveAll(
                        List.of(Message.builder().body("message one").build(),
                                Message.builder().body("message two").build()
                        )
                )
                .subscribe(s -> log.debug("saved data: {}", s));

        TimeUnit.MILLISECONDS.sleep(500);
        verifier.verify();
    }

}
