package com.example.demo

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
class Application {

    @Value("\${server.port:8080}")
    var port = 8080

    @Profile("default")
    @Bean
    fun nettyHttpServer(context: ApplicationContext): DisposableServer {
        val handler = WebHttpHandlerBuilder.applicationContext(context).build()
        val adapter = ReactorHttpHandlerAdapter(handler)
        val httpServer = HttpServer.create().host("localhost").port(this.port)
        return httpServer.handle(adapter).bindNow()
    }
}

@Throws(Exception::class)
fun main(args: Array<String>) {
    AnnotationConfigApplicationContext {
        register(Application::class.java)
        refresh()
        getBean(DisposableServer::class.java).onDispose().block()
    }
}