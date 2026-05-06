package com.example.demo;


import tools.jackson.databind.json.JsonMapper;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
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

    @Bean
    public WebClient webClient(JsonMapper objectMapper) {
        var httpAsyncClient = HttpAsyncClients.createHttp2Default();
        HttpComponentsClientHttpConnector apacheClientHttpConnector = new HttpComponentsClientHttpConnector(httpAsyncClient);

        return WebClient.builder()
                //see: https://github.com/jetty-project/jetty-reactive-httpclient
                //.clientConnector(new JettyClientHttpConnector())
                .clientConnector(apacheClientHttpConnector)
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
