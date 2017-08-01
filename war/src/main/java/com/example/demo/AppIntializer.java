package com.example.demo;

import org.springframework.web.reactive.support.AbstractAnnotationConfigDispatcherHandlerInitializer;

public class AppIntializer extends AbstractAnnotationConfigDispatcherHandlerInitializer {

    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class[]{
            WebConfig.class,
            SecurityConfig.class
        };
    }
}
