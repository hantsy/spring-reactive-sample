package com.example.demo;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSenderApplicationConfig.class)
@AutoConfigureWebTestClient
public class SenderApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RabbitMQContainer rabbitmqContainer;

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

        // 2. Instantiate Reactor RabbitMQ Receiver to consume from 'q.hello'
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitmqContainer.getHost());
        connectionFactory.setPort(rabbitmqContainer.getAmqpPort());
        connectionFactory.setUsername(rabbitmqContainer.getAdminUsername());
        connectionFactory.setPassword(rabbitmqContainer.getAdminPassword());

        ReceiverOptions receiverOptions = new ReceiverOptions()
                .connectionFactory(connectionFactory);
        Receiver receiver = RabbitFlux.createReceiver(receiverOptions);

        // 3. Assert the message content arrives in the queue
        StepVerifier.create(receiver.consumeAutoAck(SenderApplication.HELLO_QUEUE))
                .consumeNextWith(delivery -> {
                    String body = new String(delivery.getBody());
                    assertThat(body).contains(messageText);
                })
                .thenCancel()
                .verify(Duration.ofSeconds(15));
        
        receiver.close();
    }
}
