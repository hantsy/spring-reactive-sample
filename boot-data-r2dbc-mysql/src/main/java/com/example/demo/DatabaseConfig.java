/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.github.jasync.r2dbc.mysql.JasyncConnectionFactory;
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory;
import com.github.jasync.sql.db.mysql.util.URLParser;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.nio.charset.StandardCharsets;

/**
 * @author hantsy
 */
@Configuration
@EnableR2dbcRepositories
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Override
    public ConnectionFactory connectionFactory() {
        String url = "mysql://user:password@127.0.0.1:3306/test";
        return new JasyncConnectionFactory(
            new MySQLConnectionFactory(
                URLParser.INSTANCE.parseOrDie(url, StandardCharsets.UTF_8)
            )
        );
    }

}

