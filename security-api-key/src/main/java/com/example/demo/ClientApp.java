package com.example.demo;

import java.util.UUID;

record ClientApp(String name, String apiKey) {

    static ClientApp registered(String name) {
        return new ClientApp(name, UUID.randomUUID().toString());
    }

}
