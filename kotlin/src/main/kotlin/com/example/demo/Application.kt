package com.example.demo

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.ipc.netty.NettyContext
import reactor.ipc.netty.http.server.HttpServer

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
class Application {

    @Value("\${server.port:8080}")
    var port = 8080

    @Profile("default")
    @Bean
    fun nettyContext(context: ApplicationContext): NettyContext {
        val handler = WebHttpHandlerBuilder.applicationContext(context).build()
        val adapter = ReactorHttpHandlerAdapter(handler)
        val httpServer = HttpServer.create("localhost", this.port)
        return httpServer.newHandler(adapter).block()
    }
}

@Throws(Exception::class)
fun main(args: Array<String>) {
    AnnotationConfigApplicationContext {
        register(Application::class.java)
        refresh()
        getBean(NettyContext::class.java).onClose().block()
    }
}