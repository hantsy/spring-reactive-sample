package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
class ApiKeySecurityConfig {

    @Bean
    @Order(1)
    SecurityWebFilterChain apiKeySecurityWebFilterChain(ServerHttpSecurity http, ClientAppRepository clientApps) {
        return http
            .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"))
            .csrf(csrf -> csrf.disable())
            .addFilterAt(new ApiKeyAuthenticationFilter(clientApps), SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange(exchange -> exchange
                .anyExchange().hasRole("APP"))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
            .build();
    }

}
