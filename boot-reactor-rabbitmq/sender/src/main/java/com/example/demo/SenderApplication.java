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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class SenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SenderApplication.class, args);
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
    Sender sender(ConnectionFactory connectionFactory) {
        var senderOptions = new SenderOptions()
                .connectionFactory(connectionFactory)
                .connectionSupplier(cf -> cf.newConnection("sender"));
        return RabbitFlux.createSender(senderOptions);
    }
}

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
class MessageController {
    private final Sender sender;
    private final MessageRepository messageRepository;

    @PostMapping
    public Mono<ResponseEntity<Object>> sendMessage(@RequestBody Message message) {
        log.debug("sending message: {}", message);

        return messageRepository.save(message)
                .doOnSuccess(it -> {
                            var notification = "Message #" + it.id() + " was sent at " + it.sentAt();
                            var messageFlux = Flux
                                    .just(
                                            new OutboundMessage("", SenderApplication.HELLO_QUEUE, notification.getBytes())
                                    );
                            this.sender.send(messageFlux).subscribe();
                        }
                )
                .map(it -> ResponseEntity.created(URI.create("/messages/" + it.id())).build());
    }
}

@Repository
class MessageRepository {
    public static List<Message> store = new ArrayList<>();

    public Mono<Message> save(Message data) {
        var saved = new Message(UUID.randomUUID(), data.text(), LocalDateTime.now());
        store.add(saved);
        return Mono.just(saved);
    }

    public Mono<Message> findById(UUID id) {
        var found = store.stream().filter(it -> it.id().equals(id)).toList();
        return found.isEmpty() ? Mono.empty() : Mono.just(found.get(0));
    }
}

record Message(UUID id, String text, LocalDateTime sentAt) {
}