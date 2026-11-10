package com.example.demo;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .httpBasic(withDefaults())
            .addFilterBefore(apiKeyAuthenticationFilter(), SecurityWebFiltersOrder.HTTP_BASIC)
            .authorizeExchange(exchange -> exchange
                .anyExchange().authenticated())
            .build();
    }

    // Not a bean: WebHttpHandlerBuilder would also pick it up as a top-level WebFilter.
    private AuthenticationWebFilter apiKeyAuthenticationFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(
            new ApiKeyAuthenticationManager(Map.of("user-api-key", "user", "admin-api-key", "admin")));
        filter.setServerAuthenticationConverter(exchange
            -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-API-KEY"))
                .map(ApiKeyAuthentication::unauthenticated));
        return filter;
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user").password("password").roles("USER").build();
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin").password("password").roles("USER", "ADMIN").build();
        return new MapReactiveUserDetailsService(user, admin);
    }

}