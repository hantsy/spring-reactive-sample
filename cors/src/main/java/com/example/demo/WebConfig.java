package com.example.demo;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Manual CORS setup with a {@link CorsWebFilter} bean.
 * See {@link CorsWebFluxConfigurer} for the {@code WebFluxConfigurer#addCorsMappings} alternative.
 */
@Configuration
@EnableWebFlux
public class WebConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://allowed-origin.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

}