package com.example.demo;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.mariadb.r2dbc.MariadbConnectionConfiguration;
import org.mariadb.r2dbc.MariadbConnectionFactory;

public class MariadbConnectionFactories {

    static ConnectionFactory fromUrl() {
        return ConnectionFactories.get("r2dbc:mariadb://user:password@localhost/test");
    }

    static ConnectionFactory fromOptions() {
        var options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.DATABASE, "test")
                .option(ConnectionFactoryOptions.USER, "user")
                .option(ConnectionFactoryOptions.PASSWORD, "password")
                .option(ConnectionFactoryOptions.DRIVER, "mysql")
                .build();
        return ConnectionFactories.get(options);
    }

    static ConnectionFactory mariadbConnectionFactory() {
        return MariadbConnectionFactory.from(
                MariadbConnectionConfiguration.builder()
                        .host("localhost")
                        .database("blogdb")
                        .username("user")
                        .password("password")
                        .build()
        );
    }
}
