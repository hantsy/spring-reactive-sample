package com.example.demo;


import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.TomcatHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import jakarta.servlet.Servlet;
import java.io.File;

@Configuration
@ComponentScan
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
public class Application {

    @Value("${server.port:8080}")
    private int port = 8080;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);  // (1)

        Tomcat tomcatServer = context.getBean(Tomcat.class);
        tomcatServer.start();

        System.out.println("Tomcat server is running at port:" + tomcatServer.getConnector().getLocalPort());
//        System.in.read();
    }

    @Bean
    @Profile("default")
    public Tomcat embeddedTomcatServer(ApplicationContext context) {
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();

        Servlet servlet = new TomcatHttpHandlerAdapter(handler);
        Tomcat tomcat = new Tomcat();

        File base = new File(System.getProperty("java.io.tmpdir"));
        Context rootContext = tomcat.addContext("", base.getAbsolutePath());
        Tomcat.addServlet(rootContext, "main", servlet).setAsyncSupported(true);
        rootContext.addServletMappingDecoded("/", "main");

        tomcat.setHostname("localhost");
        tomcat.setPort(this.port);
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));

        return tomcat;
    }

}
