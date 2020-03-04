package com.example.demo


import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.netty.http.server.HttpServer

import static org.springframework.web.reactive.function.server.RequestPredicates.*
import static org.springframework.web.reactive.function.server.RouterFunctions.route

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.adapter.WebHttpHandlerBuilder

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
class Application {

    @Value('${server.port:8080}')
    int port = 8080

    static void main(String[] args) {

        AnnotationConfigApplicationContext context
        try {
            context = new AnnotationConfigApplicationContext(Application.class)
            context.getBean(HttpServer.class).bindNow().onDispose().block();
        } finally {
            if (context) {
                context.close()
            }
        }
    }

    @Profile("default")
    @Bean
    HttpServer nettyContext(ApplicationContext context) {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build()
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler)
        HttpServer httpServer = HttpServer.create().host("localhost").port(this.port)
        httpServer.handle(adapter)
    }

}

