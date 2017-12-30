/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.web;

import com.example.demo.web.view.MustacheResourceTemplateLoader;
import com.example.demo.web.view.MustacheViewResolver;
import com.samskivert.mustache.Mustache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

/**
 *
 * @author hantsy
 */
@Configuration
@EnableWebFlux
class WebConfig implements ApplicationContextAware, WebFluxConfigurer {

    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.ctx = context;
    }

    @Bean
    public ViewResolver mustacheViewResolver() {
        String prefix = "classpath:/templates/";
        String suffix = ".mustache";
        Mustache.TemplateLoader loader = new MustacheResourceTemplateLoader(prefix, suffix);
        MustacheViewResolver mustacheViewResolver = new MustacheViewResolver(Mustache.compiler().withLoader(loader));
        mustacheViewResolver.setPrefix(prefix);
        mustacheViewResolver.setSuffix(suffix);
        return mustacheViewResolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(mustacheViewResolver());
    }

}
