package com.example.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.server.adapter.AbstractReactiveWebInitializer;

// There is a bug to stop security WebFilter registraion before 5.0.2. 
// A workaround: https://stackoverflow.com/questions/46325632/how-to-activate-spring-security-in-a-webflux-war-application
/*
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
*/
// in Spring 5.0.2, AbstractAnnotationConfigDispatcherHandlerInitializer is marked as deprecated.
// a new AbstractReactiveWebInitializer introduced for webflux based applications.
// But it seems AbstractReactiveWebInitializer does not refresh applicationContext, override createApplicationContext() to overcome this issue temporarily.
public class AppIntializer extends AbstractReactiveWebInitializer {

	//new AnnotationConfigApplicationContext(Class<> ...) includes an extra refresh action.
    @Override
    protected ApplicationContext createApplicationContext() {
        Class<?>[] configClasses = getConfigClasses();
        Assert.notEmpty(configClasses, "No Spring configuration provided.");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(configClasses);
        return context;
    }

    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class[]{
            WebConfig.class,
            SecurityConfig.class
        };
    }

}
