package com.example.demo;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Configuration
public class ClientConfig {
    @Bean
    public PostClient postClient(WebClient webClient) {
        return new PostClient(webClient);
    }

    @Bean
    public WebClient webClient(ObjectMapper objectMapper) {
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
