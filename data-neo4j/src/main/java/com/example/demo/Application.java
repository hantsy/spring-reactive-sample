package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
public class Application {

    @Value("${server.port:8080}")
    private int port = 8080;

    public static void main(String[] args) throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            Application.class)) {
            context.getBean(HttpServer.class).bindNow().onDispose().block();
        }
    }

    @Profile("default")
    @Bean
    public HttpServer nettyHttpServer(ApplicationContext context) {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer httpServer = HttpServer.create().host("localhost").port(this.port);
        return httpServer.handle(adapter);
    }

}
