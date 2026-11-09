package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.service.RSocketServiceProxyFactory;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class ClientConfig {

    // Lazy: the TCP connection is established when the proxy is first used,
    // after Boot has started the RSocket server.
    @Bean
    @Lazy
    PostClient postClient(RSocketRequester.Builder builder) {
        var requester = builder.tcp("localhost", 7000);
        var proxyFactory = RSocketServiceProxyFactory.builder(requester)
                .blockTimeout(Duration.ofMillis(5000))
                .build();
        return proxyFactory.createClient(PostClient.class);
    }
}