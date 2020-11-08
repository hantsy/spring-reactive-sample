package com.example.demo.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.neo4j.config.AbstractReactiveNeo4jConfig;
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories;

@Configuration
@EnableReactiveNeo4jRepositories
@RequiredArgsConstructor
@Slf4j
public class DataConfig extends AbstractReactiveNeo4jConfig {

    @Value("${spring.neo4j.uri}")
    private String url;

    @Value("${spring.neo4j.authentication.username}")
    private String username;

    @Value("${spring.neo4j.authentication.password}")
    private String password;

    @Override
    @Bean
    public Driver driver() {
        log.info("driver config: {}, {}, {}", url, username, password);
        return GraphDatabase.driver(url, AuthTokens.basic(username, password));
    }

}
