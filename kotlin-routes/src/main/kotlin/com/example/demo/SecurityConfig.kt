package com.example.demo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
                .authorizeExchange()
                .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                .anyExchange().authenticated()
                .and()
                .build()
    }

    private fun currentUserMatchesPath(authentication: Mono<Authentication>, context: AuthorizationContext): Mono<AuthorizationDecision> {
        return authentication
                .map { context.variables?.get("user")?.equals(it.name) }
                .map { AuthorizationDecision(it ?: false) }
    }

    @Bean
    fun userDetailsRepository(): MapReactiveUserDetailsService {
        val rob = User.withDefaultPasswordEncoder().username("test").password("test123").roles("USER").build()
        val admin = User.withDefaultPasswordEncoder().username("admin").password("admin123").roles("USER", "ADMIN").build()
        return MapReactiveUserDetailsService(rob, admin)
    }
}