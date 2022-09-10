package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Slf4j
public class ReceiverApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiverApplication.class, args);
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

    @Bean
    Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @Bean
    KafkaReceiver<Integer, String> receiver() {
        var receiverOptions = ReceiverOptions.<Integer, String>create(consumerProps())
                .subscription(Collections.singleton(HELLO_TOPIC))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));

        return KafkaReceiver.create(receiverOptions);
    }
}

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
class EventController {
    private final KafkaReceiver<Integer, String> receiver;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> events() {
        return receiver.receive()
                .map(record -> {
                            ReceiverOffset offset = record.receiverOffset();
                            var value = record.value();
                            log.debug("Received message: topic-partition={} offset={} timestamp={} key={} value={}",
                                    offset.topicPartition(),
                                    offset.offset(),
                                    LocalDateTime.now(),
                                    record.key(),
                                    value);
                            offset.acknowledge();
                            return value;
                        }
                );
    }
}
