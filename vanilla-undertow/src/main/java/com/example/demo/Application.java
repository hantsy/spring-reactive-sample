package com.example.demo;

import io.undertow.Undertow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.UndertowHttpHandlerAdapter;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

@Configuration
@ComponentScan
@EnableWebFlux
public class Application {

    @Value("${server.port:8080}")
    private int port = 8080;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);  // (1)
        Undertow server = context.getBean(Undertow.class);
        server.start();
        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

    @Bean
    public Undertow undertowServer(ApplicationContext context) {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();  // (2)

        // Undertow
        UndertowHttpHandlerAdapter undertowAdapter = new UndertowHttpHandlerAdapter(handler);
        Undertow server = Undertow.builder()
            .addHttpListener(port, "localhost")
            .setHandler(undertowAdapter)
            .build();

        return server;
    }

}
