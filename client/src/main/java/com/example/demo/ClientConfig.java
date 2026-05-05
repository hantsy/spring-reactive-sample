package com.example.demo;

import tools.jackson.databind.json.JsonMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Configuration
public class ClientConfig {
    @Bean
    PostClient postClient(WebClient webClient) {
        return new PostClient(webClient);
    }

    @Bean
    WebClient webClient(JsonMapper objectMapper) {
        var reactorHttpClient = HttpClient.create()
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        ReactorClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector(
                reactorHttpClient);

        return WebClient.builder()
                // see: https://github.com/jetty-project/jetty-reactive-httpclient
                // .clientConnector(new JettyClientHttpConnector())
                .clientConnector(reactorClientHttpConnector)
                .codecs(
                        clientCodecConfigurer -> {
                            // use defaultCodecs() to apply DefaultCodecs
                            // clientCodecConfigurer.defaultCodecs();

                            // alter a registered encoder/decoder based on the default
                            // config.
                            // clientCodecConfigurer.defaultCodecs().jackson2Encoder(...)

                            // Or
                            // use customCodecs to register Codecs from scratch.
                            clientCodecConfigurer.customCodecs()
                                    .register(new JacksonJsonDecoder(objectMapper));
                            clientCodecConfigurer.customCodecs()
                                    .register(new JacksonJsonEncoder(objectMapper));
                        }

                )
                .exchangeStrategies(ExchangeStrategies.withDefaults())
                // .exchangeFunction(ExchangeFunctions.create(new ReactorClientHttpConnector())
                // .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {})))
                // .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
                // {clientRequest.}))
                // .defaultHeaders(httpHeaders -> httpHeaders.addAll())
                .baseUrl("http://localhost:8080")
                .build();
    }
}
