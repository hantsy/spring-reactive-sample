package com.example.demo;

import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class WebClientConfig {

    @Bean
    WebClientCustomizer webClientCustomizer(JsonMapper mapper) {
        return builder -> {
            builder
                    // configure baseUrl in application.properties
                    //.baseUrl("http://localhost:9090")
                    .codecs(c -> c.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(mapper)))
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        };
    }

    @Bean
    ClientHttpConnectorBuilder<?> clientHttpConnectorBuilder() {
        return ClientHttpConnectorBuilder.reactor();
        //.withHttpClientCustomizer(builder -> builder.proxy(...))
    }


// customize the configured client http connector.
//    @Bean
//    ClientHttpConnectorBuilderCustomizer  clientHttpConnectorBuilderCustomizer() {
//        return builder -> {};
//    }
}
