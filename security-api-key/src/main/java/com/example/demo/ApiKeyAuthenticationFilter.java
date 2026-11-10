package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class ApiKeyAuthenticationFilter extends AuthenticationWebFilter {

    ApiKeyAuthenticationFilter(ClientAppRepository clientApps) {
        super(new ApiKeyAuthenticationManager(clientApps));
        setServerAuthenticationConverter(new ApiKeyAuthenticationConverter());
        setRequiresAuthenticationMatcher(this::apiKeyPresent);
        setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(
            new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));
    }

    private String extractApiKeyFromHttpHeaders(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(ApiKeyAuthenticationConverter.API_KEY_HEADER);
    }

    private Mono<ServerWebExchangeMatcher.MatchResult> apiKeyPresent(ServerWebExchange exchange) {
        return StringUtils.hasText(extractApiKeyFromHttpHeaders(exchange))
            ? ServerWebExchangeMatcher.MatchResult.match()
            : ServerWebExchangeMatcher.MatchResult.notMatch();
    }

}
