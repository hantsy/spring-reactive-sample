package com.example.demo;

import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Alternative CORS configuration via {@link WebFluxConfigurer#addCorsMappings(CorsRegistry)},
 * applied at the handler-mapping level instead of a {@code CorsWebFilter}.
 *
 * <p>Deliberately not registered as a Spring configuration. Annotate it with
 * {@code @Configuration} (or {@code @Import} it from {@link WebConfig}) to activate it
 * in place of the {@code CorsWebFilter} bean.
 */
public class CorsWebFluxConfigurer implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://allowed-origin.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}