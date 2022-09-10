package com.example.demo;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
public class SenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SenderApplication.class, args);
    }

    public static final String HELLO_TOPIC = "hello";

    @Autowired
    KafkaAdmin kafkaAdmin;

    @PostConstruct
    public void init() {
        kafkaAdmin.createOrModifyTopics(
                TopicBuilder.name(HELLO_TOPIC)
                        .partitions(1)
                        .replicas(1)
                        .build()
        );
    }

    private Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    KafkaSender<Integer, String> sender() {
        var senderOptions = SenderOptions.<Integer, String>create(producerProps());
        return KafkaSender.create(senderOptions);
    }
}

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
class MessageController {
    private final KafkaSender<Integer, String> sender;
    private final MessageRepository messageRepository;

    @PostMapping
    public Mono<ResponseEntity<Object>> sendMessage(@RequestBody Message message) {
        log.debug("sending message: {}", message);
        Integer key = new SecureRandom().nextInt(Integer.MAX_VALUE);
        return messageRepository.save(message)
                .doOnSuccess(it -> {
                            var notification = "Message #" + it.id() + " was sent at " + it.sentAt();
                            this.sender.send(Flux.just(SenderRecord.create(new ProducerRecord<>(SenderApplication.HELLO_TOPIC, key, notification), key)))
                                    .doOnError(e -> log.error("Send failed", e))
                                    .subscribe(r -> {
                                        RecordMetadata metadata = r.recordMetadata();
                                        log.debug("Message {} sent successfully, topic-partition={}-{} offset={} timestamp={}",
                                                r.correlationMetadata(),
                                                metadata.topic(),
                                                metadata.partition(),
                                                metadata.offset(),
                                                LocalDateTime.now()
                                        );
                                    });
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