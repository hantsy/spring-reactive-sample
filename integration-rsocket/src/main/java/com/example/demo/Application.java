package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.ServerRSocketConnector;
import org.springframework.integration.rsocket.ServerRSocketMessageHandler;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@SpringBootApplication
@EnableIntegration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // see PR: https://github.com/spring-projects/spring-boot/pull/18834
    @Bean
    ServerRSocketMessageHandler serverRSocketMessageHandler(RSocketStrategies rSocketStrategies) {
        var handler = new ServerRSocketMessageHandler(true);
        handler.setRSocketStrategies(rSocketStrategies);
        return handler;
    }

    @Bean
    public ServerRSocketConnector serverRSocketConnector(ServerRSocketMessageHandler serverRSocketMessageHandler) {
        return new ServerRSocketConnector(serverRSocketMessageHandler);
    }

    @Bean
    public IntegrationFlow rsocketUpperCaseFlow(ServerRSocketConnector serverRSocketConnector) {
        return IntegrationFlow
                .from(RSockets.inboundGateway("/uppercase")
                        .requestElementType(ResolvableType.forClass(String.class))
                        .interactionModels(RSocketInteractionModel.requestChannel)
                        .rsocketConnector(serverRSocketConnector)
                )
                .<Flux<String>, Flux<String>>transform((flux) -> flux.map(String::toUpperCase))
                .get();
    }

    @Bean
    public ClientRSocketConnector clientRSocketConnector() {
        ClientRSocketConnector clientRSocketConnector = new ClientRSocketConnector("localhost", 7000);
        clientRSocketConnector.setAutoStartup(false);
        return clientRSocketConnector;
    }

    @Bean
    public IntegrationFlow rsocketUpperCaseRequestFlow(ClientRSocketConnector clientRSocketConnector) {
        return IntegrationFlow
                .from(Function.class)
                .handle(RSockets.outboundGateway("/uppercase")
                        .interactionModel((message) -> RSocketInteractionModel.requestChannel)
                        .expectedResponseType("T(java.lang.String)")
                        .clientRSocketConnector(clientRSocketConnector))
                .get();
    }
}
