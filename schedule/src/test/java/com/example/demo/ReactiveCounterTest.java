package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(value = ReactiveCounterTest.TestConfig.class)
public class ReactiveCounterTest {

    @Configuration
    @ComponentScan(basePackageClasses = {ScheduleConfig.class})
    static class TestConfig {
    }

    @Autowired
    ReactiveCounter counter;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void givenSleepBy100ms_whenGetInvocationCount_thenIsGreaterThanZero()
            throws InterruptedException {
        Thread.sleep(100L);
        counter.getInvocationCount()
                .as(StepVerifier::create)
                .consumeNextWith(count -> assertThat(count).isGreaterThan(0))
                .thenCancel()
                .verify();
    }

}
