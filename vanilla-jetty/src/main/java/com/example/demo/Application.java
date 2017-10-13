package com.example.demo;

import javax.servlet.Servlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.JettyHttpHandlerAdapter;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

@Configuration
@ComponentScan
@EnableWebFlux
public class Application {

    @Value("${server.port:8080}")
    private int port = 8080;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);  // (1)
        Server server = context.getBean(Server.class);
        server.start();
        server.join();

        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

    @Bean
    public Server jettyServer(ApplicationContext context) throws Exception {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
        Servlet servlet = new JettyHttpHandlerAdapter(handler);

        Server server = new Server();
        ServletContextHandler contextHandler = new ServletContextHandler(server, "");
        contextHandler.addServlet(new ServletHolder(servlet), "/");
        contextHandler.start();

        ServerConnector connector = new ServerConnector(server);
        connector.setHost("localhost");
        connector.setPort(port);
        server.addConnector(connector);

        return server;
    }

}
