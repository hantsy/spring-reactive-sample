package com.example.demo.domain;

import com.example.demo.Greeting;
import com.example.demo.GreetingListener;
import com.example.demo.GreetingPublisher;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = TypedGreetingEventTest.TestConfig.class)
@RecordApplicationEvents
public class TypedGreetingEventTest {

    @Autowired
    ApplicationEvents applicationEvents;

    @Autowired
    GreetingPublisher publisher;

    @Autowired
    GreetingListener listener;

    @Configuration
    @ComponentScan(basePackageClasses = Greeting.class)
    static class TestConfig {

    }

    @Test
    public void testGreetingEvents() {
        publisher.publishGreetingEvent("hello world");

        Awaitility.await().atMost(Duration.ofMillis(500))
                .untilAsserted(() ->
                        assertThat(applicationEvents.stream(Greeting.class).count()).isEqualTo(1)
                );


        Awaitility.await().atMost(Duration.ofMillis(1000))
                .untilAsserted(() ->
                        StepVerifier.create(listener.sinks.asFlux())
                                .consumeNextWith(greeting -> assertThat(greeting.message()).isEqualTo("hello world"))
                                .thenCancel()
                                .verify()
                );
    }
}
