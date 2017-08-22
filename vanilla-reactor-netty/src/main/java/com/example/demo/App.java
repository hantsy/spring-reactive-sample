package com.example.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.DispatcherHandler;
import reactor.ipc.netty.http.server.HttpServer;

public class App {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);  // (1)

        HttpHandler handler = DispatcherHandler.toHttpHandler(context);  // (2)
        
        // Reactor Netty
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer.create(DEFAULT_HOST, DEFAULT_PORT).newHandler(adapter).block();

        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

}
