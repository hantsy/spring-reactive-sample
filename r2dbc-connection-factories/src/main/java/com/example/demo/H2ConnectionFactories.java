package com.example.demo;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;

public class H2ConnectionFactories {
    static ConnectionFactory fromUrl() {
        return ConnectionFactories.get("r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    }

    static ConnectionFactory inMemory() {
//        H2ConnectionFactory connectionFactory = new H2ConnectionFactory(H2ConnectionConfiguration.builder()
//                .inMemory("...")
//                .property(H2ConnectionOption.DB_CLOSE_DELAY, "-1")
//                .build());
        return H2ConnectionFactory.inMemory("test");
    }

    static ConnectionFactory file() {
        return new H2ConnectionFactory(
                H2ConnectionConfiguration.builder()
                        //.inMemory("testdb")
                        .file("./testdb")
                        .username("sa")
                        .password("password")
                        .build()
        );
    }

}
