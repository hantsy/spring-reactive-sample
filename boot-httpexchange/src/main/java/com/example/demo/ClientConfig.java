package com.example.demo;

import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;

@Configuration(proxyBeanMethods = false)
public class ClientConfig {

    @Bean
    WebClientCustomizer webClientCustomizer() {
        return builder -> builder
                // throw an exception and handle it in the calling process
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, ClientResponse::createError);

        // alternatively, wrap it into a client-friendly exception:
        // .defaultStatusHandler(HttpStatusCode::is4xxClientError,
        //         response -> response.createException()
        //                 .map(it -> new PostServiceException(it.getResponseBodyAsString())))

        // or restore the original exception from the ProblemDetail body:
        // .defaultStatusHandler(status -> status == HttpStatus.NOT_FOUND,
        //         response -> response.createException()
        //                 .map(it -> {
        //                     var problemDetail = jsonMapper.readValue(it.getResponseBodyAsByteArray(), ProblemDetail.class);
        //                     return new PostNotFoundException(UUID.fromString(problemDetail.getProperties().get("id").toString()));
        //                 }))
    }
}