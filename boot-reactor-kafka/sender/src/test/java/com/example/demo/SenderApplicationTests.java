package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.kafka.KafkaContainer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSenderApplicationConfig.class)
@AutoConfigureWebTestClient
public class SenderApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private KafkaContainer kafkaContainer;

    @Test
    public void testSendMessage() {
        String messageText = "Test Message " + System.currentTimeMillis();
        Message payload = new Message(null, messageText, null);

        // 1. Invoke POST /messages
        webTestClient.post()
                .uri("/messages")
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().consumeWith(result -> {
                    String location = result.getResponseHeaders().getFirst("Location");
                    assertThat(location).startsWith("/messages/");
                });

        // 2. Instantiate KafkaReceiver to consume from 'hello' topic
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ReceiverOptions<Integer, String> receiverOptions = ReceiverOptions.<Integer, String>create(consumerProps)
                .subscription(Collections.singleton(SenderApplication.HELLO_TOPIC));

        KafkaReceiver<Integer, String> receiver = KafkaReceiver.create(receiverOptions);

        // 3. Assert the published message arrives in Kafka
        waitAtMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            StepVerifier.create(receiver.receive())
                    .consumeNextWith(record -> {
                        assertThat(record.value()).contains(messageText);
                    })
                    .thenCancel()
                    .verify(Duration.ofSeconds(5));
        });
    }
}
