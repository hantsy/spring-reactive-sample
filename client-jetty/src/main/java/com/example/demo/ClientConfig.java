package com.example.demo;


import tools.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.eclipse.jetty.client.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class ClientConfig {
    @Bean
    public PostClient postClient(WebClient webClient) {
        return new PostClient(webClient);
    }

    @SneakyThrows
    @Bean
    public WebClient webClient(JsonMapper objectMapper) {
        var jettyHttpClient = new HttpClient();
        jettyHttpClient.setConnectTimeout(5000L);
        jettyHttpClient.start();
        JettyClientHttpConnector jettyClientHttpConnector = new JettyClientHttpConnector(jettyHttpClient);

        return WebClient.builder()
                //see: https://github.com/jetty-project/jetty-reactive-httpclient
                //.clientConnector(new JettyClientHttpConnector())
                .clientConnector(jettyClientHttpConnector)
                .codecs(
                        clientCodecConfigurer -> {
                            // use defaultCodecs() to apply DefaultCodecs
                            // clientCodecConfigurer.defaultCodecs();

                            // alter a registered encoder/decoder based on the default config.
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
//                .exchangeFunction(ExchangeFunctions.create(new ReactorClientHttpConnector())
//                        .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {})))
//                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {clientRequest.}))
                //.defaultHeaders(httpHeaders -> httpHeaders.addAll())
                .baseUrl("http://localhost:8080")
                .build();
    }
}
