package com.example.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ClientConfig {

    @Bean
    JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .findAndAddModules()
                .build();
    }

    @Bean
    WebClient webClient(JsonMapper mapper) {
        var reactorHttpClient = HttpClient.create()
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        var clientConnector = new ReactorClientHttpConnector(reactorHttpClient);

        return WebClient.builder()
                .clientConnector(clientConnector)
                .codecs(clientCodecConfigurer -> {
                    clientCodecConfigurer.customCodecs()
                            .register(new JacksonJsonDecoder(mapper));
                    clientCodecConfigurer.customCodecs()
                            .register(new JacksonJsonEncoder(mapper));
                })
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("http://localhost:9090")
                .build();
    }

    // Manual replacement for Spring Boot's @ImportHttpServices auto-configuration:
    // create the @HttpExchange proxy from the WebClient-backed exchange adapter.
    @Bean
    PostHttpService postHttpService(WebClient webClient) {
        var proxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
        return proxyFactory.createClient(PostHttpService.class);
    }
}