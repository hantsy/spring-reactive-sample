package com.example.demo;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestReceiverApplicationConfig.class)
@AutoConfigureWebTestClient
public class ReceiverApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RabbitMQContainer rabbitmqContainer;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Test
    public void testEventsStream() {
        String testMessage = "Hello RabbitMQ Test " + System.currentTimeMillis();

        // 1. Ensure the q.hello queue is declared
        amqpAdmin.declareQueue(new Queue(ReceiverApplication.HELLO_QUEUE, false, false, true));

        // 2. Instantiate Reactor RabbitMQ Sender to publish a message
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitmqContainer.getHost());
        connectionFactory.setPort(rabbitmqContainer.getAmqpPort());
        connectionFactory.setUsername(rabbitmqContainer.getAdminUsername());
        connectionFactory.setPassword(rabbitmqContainer.getAdminPassword());

        SenderOptions senderOptions = new SenderOptions()
                .connectionFactory(connectionFactory);
        Sender sender = RabbitFlux.createSender(senderOptions);

        sender.send(Flux.just(new OutboundMessage("", ReceiverApplication.HELLO_QUEUE, testMessage.getBytes())))
                .subscribe();

        // 3. Connect to GET /events and assert SSE stream
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
