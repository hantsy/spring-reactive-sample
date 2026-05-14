package com.example.demo;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.kafka.KafkaContainer;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestReceiverApplicationConfig.class)
@AutoConfigureWebTestClient
public class ReceiverApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private KafkaContainer kafkaContainer;

    @Test
    public void testEventsStream() {
        String testMessage = "Hello from Test " + System.currentTimeMillis();

        // 1. Instantiate KafkaSender to publish a message
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        SenderOptions<Integer, String> senderOptions = SenderOptions.create(producerProps);
        KafkaSender<Integer, String> sender = KafkaSender.create(senderOptions);

        sender.send(Flux.just(SenderRecord.create(new ProducerRecord<>(ReceiverApplication.HELLO_TOPIC, 1, testMessage), 1)))
                .doOnError(e -> System.err.println("Send failed: " + e.getMessage()))
                .subscribe();

        // 2. Connect to GET /events and assert SSE stream
        webTestClient.get()
                .uri("/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextMatches(received -> received.equals(testMessage))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
                
        sender.close();
    }
}
