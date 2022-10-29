package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;


@Slf4j
public class ReactorByExample {

    @Test
    public void testSwitchOnFirst() {
        var flux = Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        flux.switchOnFirst((signal, integerFlux) -> {
                            log.debug("signal: {}", signal);
                            if (signal.hasValue()) {
                                Integer data = signal.get();
                                log.debug("signal data: {}", data);
                                log.debug("data %2: {}", data % 2);
                                if (data % 2 == 0) {
                                    log.debug("return value: {}", data);
                                    return Flux.concat(Flux.just(0), integerFlux.filter(i -> i != data));
                                } else {
                                    log.debug("return value* 2: {}", data);

                                    return Flux.concat(Flux.just(data * 2), integerFlux.filter(i -> i != data));
                                }
                            }
                            log.debug("not found signal, use default:");
                            return flux;
                        }
                )
                .subscribe(integer -> log.debug("received: {}", integer),
                        error -> log.error("error:" + error.getMessage()),
                        () -> log.debug("done")
                );
    }

    @Test
    public void testSwitchIfEmpty() {
        var flux = Flux.empty();
        flux.switchIfEmpty(Flux.just(1, 2, 3, 4))
                .subscribe(integer -> log.debug("received: {}", integer),
                        error -> log.error("error:" + error.getMessage()),
                        () -> log.debug("done")
                );
    }

}
