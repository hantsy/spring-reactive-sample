/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import freemarker.template.utility.XmlEscape;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
import java.util.Map;

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
    public FreeMarkerConfigurer freeMarkerConfig() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("xml_escape", new XmlEscape());

        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setPreferFileSystemAccess(false);
        configurer.setTemplateLoaderPath("classpath:/templates");
        configurer.setResourceLoader(this.ctx);
        configurer.setFreemarkerVariables(variables);

        return configurer;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.freeMarker();
    }

}
