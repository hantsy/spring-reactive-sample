package com.example.demo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.DispatcherHandler;

public class App {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);  // (1)

        HttpHandler handler = DispatcherHandler.toHttpHandler(context);  // (2)

        // Tomcat and Jetty (also see notes below)
        ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(handler);

        Server server = new Server(DEFAULT_PORT);
        
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setErrorHandler(null);
        contextHandler.setContextPath("");
        contextHandler.addServlet(new ServletHolder(servlet), "/");
        
        server.setHandler(contextHandler);
        server.start();
        server.join();
    

        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

}
