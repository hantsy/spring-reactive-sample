package com.example.demo


import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

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
import reactor.ipc.netty.NettyContext
import reactor.ipc.netty.http.server.HttpServer

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
class Application {

    @Value('${server.port:8080}')
    int port = 8080

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context
        try {
            context = new AnnotationConfigApplicationContext(Application.class)
            context.getBean(NettyContext.class).onClose().block()
        }finally {
            if(context){context.close()}
        }
    }

    @Profile("default")
    @Bean
    NettyContext nettyContext(ApplicationContext context) {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer httpServer = HttpServer.create("localhost", this.port);
        return httpServer.newHandler(adapter).block();
    }

    @Bean
    RouterFunction<ServerResponse> routes(PostHandler postHandler) {
        return route(GET("/posts"), postHandler.&all)
                .andRoute(POST("/posts"), postHandler.&create)
                .andRoute(GET("/posts/{id}"), postHandler.&get)
                .andRoute(PUT("/posts/{id}"), postHandler.&update)
                .andRoute(DELETE("/posts/{id}"), postHandler.&delete)
    }

}

