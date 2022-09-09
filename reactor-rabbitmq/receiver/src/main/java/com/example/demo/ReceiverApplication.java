package com.example.demo;

import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ReceiverApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiverApplication.class, args);
    }

    public static final String HELLO_QUEUE = "q.hello";

    @Autowired
    AmqpAdmin amqpAdmin;

    @PostConstruct
    public void init() {
        amqpAdmin.declareQueue(new Queue(HELLO_QUEUE, false, false, true));
    }

    @Bean
    ConnectionFactory connectionFactory(RabbitProperties rabbitProperties) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitProperties.getHost());
        connectionFactory.setPort(rabbitProperties.getPort());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.useNio();
        return connectionFactory;
    }

    @Bean
    Receiver receiver(ConnectionFactory connectionFactory) {
        var receiverOptions = new ReceiverOptions()
                .connectionFactory(connectionFactory)
                .connectionSupplier(cf -> cf.newConnection("receiver"));
        return RabbitFlux.createReceiver(receiverOptions);
    }
}

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
class EventController {
    private final Receiver receiver;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> events() {
        return receiver.consumeAutoAck(ReceiverApplication.HELLO_QUEUE)
                .map(it -> new String(it.getBody()));
    }
}
