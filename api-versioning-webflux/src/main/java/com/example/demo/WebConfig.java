package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ApiVersionConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                //.usePathSegment(0)
                .useRequestHeader("X-API-Version")
                //.useRequestParam("version")

                .setDefaultVersion("1.0")
                // When a defaultVersion is also set, this is automatically set to false.
                // .setVersionRequired(true)

                // default is SemanticApiVersionParser
                //.setVersionParser(new SemanticApiVersionParser())

                // set detectSupportedVersions(false) when adding supported versions
                .detectSupportedVersions(false)
                .addSupportedVersions("1.0", "1.1", "2.0");
    }

}
