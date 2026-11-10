package com.example.demo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class ClientAppRepository {

    private final Map<String, ClientApp> byApiKey = new ConcurrentHashMap<>();

    ClientAppRepository() {
        save(new ClientApp("web-client", "web-client-api-key"));
        save(new ClientApp("mobile-client", "mobile-client-api-key"));
    }

    Mono<ClientApp> findByApiKey(String apiKey) {
        return Mono.justOrEmpty(this.byApiKey.get(apiKey));
    }

    Flux<ClientApp> findAll() {
        return Flux.fromIterable(this.byApiKey.values());
    }

    Mono<ClientApp> save(ClientApp clientApp) {
        this.byApiKey.put(clientApp.apiKey(), clientApp);
        return Mono.just(clientApp);
    }

}
