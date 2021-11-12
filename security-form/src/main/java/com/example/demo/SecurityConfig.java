/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * @author hantsy
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http.authorizeExchange()
                    .anyExchange().authenticated()
                .and()
                    .formLogin()
                .and()
                .build();
    }

    private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
                .map(a -> context.getVariables().get("user").equals(a.getName()))
                .map(granted -> new AuthorizationDecision(granted));
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsRepository() {
        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
        UserDetails user = userBuilder.username("user").password("password").roles("USER").build();
        UserDetails admin = userBuilder.username("admin").password("password").roles("USER", "ADMIN").build();
        return new MapReactiveUserDetailsService(user, admin);
    }

}
