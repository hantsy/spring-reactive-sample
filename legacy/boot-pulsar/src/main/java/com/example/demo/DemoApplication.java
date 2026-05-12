package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.pulsar.core.DefaultSchemaResolver;
import org.springframework.pulsar.core.SchemaResolver;
import org.springframework.pulsar.reactive.config.annotation.ReactivePulsarListener;
import org.springframework.pulsar.reactive.core.ReactivePulsarTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.stream.IntStream;

@SpringBootApplication
@Slf4j
public class DemoApplication {
    public static final String GREETING_TOPIC = "greetingTopic";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public SchemaResolver.SchemaResolverCustomizer<DefaultSchemaResolver> schemaResolverCustomizer() {
        return (schemaResolver) -> schemaResolver
                .addCustomSchemaMapping(Greeting.class, Schema.JSON(Greeting.class));
    }

    @Bean
    public ApplicationRunner applicationRunner(ReactivePulsarTemplate<Greeting> template) {
        return args -> {
            IntStream.range(1, 10)
                    .forEach(i ->
                            template.send(GREETING_TOPIC, new Greeting("greeting #" + i))
                                    .subscribe(id -> log.debug("sent message:{}", id))
                    );

        };
    }

}

record Greeting(String message) {
}

@Component
@Slf4j
class GreetingListener {
    public Sinks.Many<String> messages = Sinks.many().replay().latest();

    @ReactivePulsarListener(
            subscriptionName = "greetingSubscription",
            topics = {DemoApplication.GREETING_TOPIC},
            schemaType = SchemaType.JSON
    )
    public Mono<Void> onGreeting(Greeting greeting) {
        log.debug("received: {}", greeting);
        messages.emitNext(greeting.message(), Sinks.EmitFailureHandler.FAIL_FAST);
        return Mono.empty();
    }
}