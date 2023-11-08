package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
public class GreetingListener {

    public Sinks.Many<Greeting> sinks = Sinks.many().replay().latest();
    @EventListener
    public Mono<Void> onGreetingEvent(Greeting event) {
        log.debug("received event:{}", event);
        sinks.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
        return Mono.empty();
    }
}
