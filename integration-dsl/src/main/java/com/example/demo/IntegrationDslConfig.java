/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author hantsy
 */
@Configuration
@EnableIntegration
public class IntegrationDslConfig {

    @Bean
    public IntegrationFlow inboundChannelAdapterFlow() {
        return IntegrationFlows
            .from(
                WebFlux
                    .inboundGateway("/all")
                    .requestMapping(m -> m.methods(HttpMethod.GET))
                    // .requestPayloadType(ResolvableType.forClassWithGenerics(Flux.class, String.class))
                    //.statusCodeFunction(m -> HttpStatus.OK)
            )
            .channel(c -> c.flux("outboundReactive.input"))
            .get();
    }

//    @Bean
//    public MessageChannel fluxResultChannel() {
//        return new FluxMessageChannel();
//    }
    @Bean
    public IntegrationFlow outboundReactive() {
        return f -> f
            .handle(
                WebFlux
                    .<MultiValueMap<String, String>>outboundGateway(
                        m -> UriComponentsBuilder
                            .fromUriString("http://localhost:8080/posts")
                            //.queryParams(m.getPayload())
                            .build()
                            .toUri()
                    )
                    .httpMethod(HttpMethod.GET)
                    .expectedResponseType(String.class)
            );
    }

}
