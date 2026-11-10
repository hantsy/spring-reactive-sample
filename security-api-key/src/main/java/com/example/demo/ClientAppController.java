package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/clients")
class ClientAppController {

    record Registration(String name) {
    }

    private final ClientAppRepository clientApps;

    ClientAppController(ClientAppRepository clientApps) {
        this.clientApps = clientApps;
    }

    @GetMapping
    Flux<ClientApp> list() {
        return this.clientApps.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<ClientApp> register(@RequestBody Mono<Registration> registration) {
        return registration
            .map(entry -> ClientApp.registered(entry.name()))
            .flatMap(this.clientApps::save);
    }

}
