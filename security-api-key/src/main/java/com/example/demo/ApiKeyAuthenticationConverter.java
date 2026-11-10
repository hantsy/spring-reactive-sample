package com.example.demo;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class ApiKeyAuthenticationConverter implements ServerAuthenticationConverter {

    public static final String API_KEY_HEADER = "X-API-KEY";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER))
            .map(ApiKeyAuthentication::unauthenticated);
    }

}
