package com.example.demo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers

@Configuration(proxyBeanMethods = false)
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http {
            csrf { disable() }
            httpBasic {}
            authorizeExchange {
                authorize("/auth/**", authenticated)
                authorize(pathMatchers(HttpMethod.GET, "/posts/**"), permitAll)
                authorize(pathMatchers(HttpMethod.DELETE, "/posts/**"), hasRole("ADMIN"))
                authorize("/posts/**", authenticated)
                authorize(anyExchange, permitAll)
            }
        }

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): MapReactiveUserDetailsService {
        val user = User.withUsername("user")
            .password("password")
            .passwordEncoder { passwordEncoder.encode(it) }
            .roles("USER")
            .build()
        val admin = User.withUsername("admin")
            .password("password")
            .passwordEncoder { passwordEncoder.encode(it) }
            .roles("USER", "ADMIN")
            .build()
        return MapReactiveUserDetailsService(user, admin)
    }
}