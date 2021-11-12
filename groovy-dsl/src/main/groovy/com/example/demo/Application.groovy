package com.example.demo

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.context.support.GenericGroovyApplicationContext
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.netty.http.server.HttpServer

@ComponentScan
@Configuration
@ImportResource("classpath:application.groovy")
class Application {

    static void main(String[] args) {

        AnnotationConfigApplicationContext context
        try {
            context = new AnnotationConfigApplicationContext(Application.class)
            context.getBean(HttpServer.class).bindNow().onDispose().block()
        } finally {
            if (context) {
                context.close()
            }
        }
    }

    @Bean
    HttpServer nettyServer(ApplicationContext context) {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build()
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler)
        HttpServer.create().host("localhost").port(8080).handle(adapter)
    }

}

