package com.example.demo;

import jakarta.servlet.Servlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.JettyHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
public class Application {

    @Value("${server.port:8080}")
    private int port = 8080;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);  // (1)
        Server server = context.getBean(Server.class);
        server.start();
        //server.join();

        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

    @Bean
    public Server jettyServer(ApplicationContext context) throws Exception {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
        Servlet servlet = new JettyHttpHandlerAdapter(handler);

        Server server = new Server(8080);

        ServletContextHandler contextHandler = new ServletContextHandler("");
        contextHandler.addServlet(new ServletHolder(servlet), "/*");
        server.setHandler(contextHandler);

        return server;
    }

}
