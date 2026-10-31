package com.example.demo;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

public class OracleConnectionFactories {
    
    static ConnectionFactory fromUrl() {
        return ConnectionFactories.get("r2dbc:oracle://SYS:Passw0rd@localhost:1521/XE");
    }
    
    static ConnectionFactory fromOptions() {
        var options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.DATABASE, "XE")
                .option(ConnectionFactoryOptions.USER, "SYS")
                .option(ConnectionFactoryOptions.PASSWORD, "Passw0rd")
                .option(ConnectionFactoryOptions.DRIVER, "oracle")
                //.option(PostgresqlConnectionFactoryProvider.OPTIONS, Map.of("lock_timeout", "30s"))
                .build();
        return ConnectionFactories.get(options);
    }
}
