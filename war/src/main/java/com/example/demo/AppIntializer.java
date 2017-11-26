package com.example.demo;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.support.AbstractAnnotationConfigDispatcherHandlerInitializer;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

// There is a bug to stop security registraion before 5.0.2. 
// https://stackoverflow.com/questions/46325632/how-to-activate-spring-security-in-a-webflux-war-application
public class AppIntializer extends AbstractAnnotationConfigDispatcherHandlerInitializer {

    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class[]{
            WebConfig.class,
            SecurityConfig.class
        };
    }

    @Override
    protected void registerDispatcherHandler(ServletContext servletContext) {
        String servletName = getServletName();
        ApplicationContext applicationContext = createApplicationContext();

        refreshApplicationContext(applicationContext);
        registerCloseListener(servletContext, applicationContext);

        HttpHandler httpHandler = WebHttpHandlerBuilder.applicationContext(applicationContext)
            .build();
        ServletHttpHandlerAdapter handlerAdapter = new ServletHttpHandlerAdapter(httpHandler);

        ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, handlerAdapter);

        registration.setLoadOnStartup(1);
        registration.addMapping(getServletMapping());
        registration.setAsyncSupported(true);

        customizeRegistration(registration);
    }
}
