/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.webflux.inbound.WebFluxInboundEndpoint;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.webflux.outbound.WebFluxRequestExecutingMessageHandler;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * @author hantsy
 */
@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Bean
    public WebFluxInboundEndpoint jsonInboundEndpoint() {
        WebFluxInboundEndpoint endpoint = new WebFluxInboundEndpoint();
        RequestMapping requestMapping = new RequestMapping();
        requestMapping.setPathPatterns("/all");
        endpoint.setRequestMapping(requestMapping);
        endpoint.setRequestChannel(fluxResultChannel());
        return endpoint;
    }

    @Bean
    public MessageChannel fluxResultChannel() {
        return new FluxMessageChannel();
    }
    
    @Bean
    public WebClient webClient(){
        return WebClient.create();
    }

    @ServiceActivator(inputChannel = "fluxResultChannel")
    @Bean
    public WebFluxRequestExecutingMessageHandler reactiveOutbound(WebClient client) {
        WebFluxRequestExecutingMessageHandler handler
            = new WebFluxRequestExecutingMessageHandler("http://localhost:8080/posts", client);
        handler.setHttpMethod(HttpMethod.GET);
        handler.setExpectedResponseType(String.class);
        return handler;
    }

}
