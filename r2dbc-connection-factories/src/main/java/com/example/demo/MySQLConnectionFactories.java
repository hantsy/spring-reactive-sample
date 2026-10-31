package com.example.demo;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

public class MySQLConnectionFactories {

    static ConnectionFactory fromUrl() {
        return ConnectionFactories.get("r2dbc:mysql://user:password@localhost/test");
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

    static ConnectionFactory mysqlConnectionFactory() {
        return MySqlConnectionFactory.from(
                MySqlConnectionConfiguration.builder()
                        .host("localhost")
                        .database("blogdb")
                        .username("user")
                        .password("password")
                        .build()
        );
    }
}
