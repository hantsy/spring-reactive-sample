package com.example.demo;

import io.undertow.Undertow;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.UndertowHttpHandlerAdapter;
import org.springframework.web.reactive.DispatcherHandler;

public class App {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class, SecurityConfig.class);  // (1)

        HttpHandler handler = DispatcherHandler.toHttpHandler(context);  // (2)

        // Undertow
        UndertowHttpHandlerAdapter undertowAdapter = new UndertowHttpHandlerAdapter(handler);
        Undertow server = Undertow.builder().addHttpListener(DEFAULT_PORT, DEFAULT_HOST).setHandler(undertowAdapter).build();
        server.start();
        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

}
