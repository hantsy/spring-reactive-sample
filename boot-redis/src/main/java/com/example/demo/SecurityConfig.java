package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(it -> it.securityContextRepository(NoOpServerSecurityContextRepository.getInstance()))
                .authorizeExchange(it -> it.pathMatchers("/posts/**").authenticated()
                        .anyExchange().permitAll())
//                        .pathMatchers(HttpMethod.POST, "/posts/**").authenticated()
//                        .pathMatchers(HttpMethod.DELETE, "/posts/**").authenticated()
//                        .anyExchange().permitAll())
                .build();
    }
}
