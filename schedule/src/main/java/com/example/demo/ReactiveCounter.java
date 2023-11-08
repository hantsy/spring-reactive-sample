package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class ReactiveCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    @Scheduled(fixedDelay = 5)
    public Mono<Void> scheduled() {
        return Mono.defer(() -> Mono.just(count.incrementAndGet()))
                .doOnNext(item -> log.debug("current count:{}", item))
                .then();
    }

    public Mono<Integer> getInvocationCount() {
        return Mono.just(this.count.get());
    }

}