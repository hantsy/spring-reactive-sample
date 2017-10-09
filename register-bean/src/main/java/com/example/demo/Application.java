package com.example.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.ipc.netty.NettyContext;
import reactor.ipc.netty.http.server.HttpServer;

public class Application {

   public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        PostRepository posts = new PostRepository();
        PostHandler postHandler = new PostHandler(posts);
        Routes routesBean = new Routes(postHandler);

        context.registerBean(PostRepository.class, () -> posts);
        context.registerBean(PostHandler.class, () -> postHandler);
        context.registerBean(Routes.class, () -> routesBean);
        context.registerBean(WebHandler.class, () -> RouterFunctions.toWebHandler(routesBean.routes(), HandlerStrategies.builder().build()));
        context.refresh();

        nettyContext(context).onClose().block();
    }

    public static NettyContext nettyContext(ApplicationContext context) {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer httpServer = HttpServer.create("localhost", 8080);
        return httpServer.newHandler(adapter).block();
    }

}
