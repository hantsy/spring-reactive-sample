package com.example.demo;

import jakarta.servlet.Servlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.http.server.reactive.ContextPathCompositeHandler;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.JettyCoreHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import java.util.Map;

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
public class Application {

    @Value("${server.port:8080}")
    private int port = 8080;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);  // (1)
        Server server = context.getBean(Server.class);

        // 4. Starts up application
        server.start();
        //server.join();

        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

    @Bean
    public Server jettyServer(ApplicationContext context) throws Exception {
        // 1. Initialize the adapter with Spring's HttpHandler
        HttpHandler httpHandler = WebHttpHandlerBuilder.applicationContext(context).build();
        // 2. Create the Jetty Core adapter
        JettyCoreHttpHandlerAdapter adapter = new JettyCoreHttpHandlerAdapter(httpHandler);

        // 3. Set up the Jetty 12 Core Path Mappings
        PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();

        // CRITICAL JETTY 12 CHANGE: Bind your adapter explicitly via PathSpec
        // Use "/*" to ensure all sub-paths underneath the root context are matched correctly
        pathMappingsHandler.addMapping(PathSpec.from("/*"), adapter);

        // 4. Initialize server and attach the mapping handler
        Server server = new Server();
        server.setHandler(pathMappingsHandler);

        // 5. Setup Network Connector
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        return server;
    }

}
