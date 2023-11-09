package com.example.demo;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;


@Configuration
public class ClientConfig {
    @Bean
    public PostClient postClient(WebClient webClient) {
        return new PostClient(webClient);
    }

    @Bean
    public WebClient webClient(ObjectMapper objectMapper) {
        var jvmHttpClient = HttpClient.newBuilder()
                .executor(Executors.newCachedThreadPool())
                .version(HttpClient.Version.HTTP_2)
                .build();
        var jdkClientHttpConnector = new JdkClientHttpConnector(jvmHttpClient);

        return WebClient.builder()
                //see: https://github.com/jetty-project/jetty-reactive-httpclient
                //.clientConnector(new JettyClientHttpConnector())
                .clientConnector(jdkClientHttpConnector)
                .codecs(
                        clientCodecConfigurer -> {
                            // use defaultCodecs() to apply DefaultCodecs
                            // clientCodecConfigurer.defaultCodecs();

                            // alter a registered encoder/decoder based on the default config.
                            // clientCodecConfigurer.defaultCodecs().jackson2Encoder(...)

                            // Or
                            // use customCodecs to register Codecs from scratch.
                            clientCodecConfigurer.customCodecs()
                                    .register(new Jackson2JsonDecoder(objectMapper));
                            clientCodecConfigurer.customCodecs()
                                    .register(new Jackson2JsonEncoder(objectMapper));
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
