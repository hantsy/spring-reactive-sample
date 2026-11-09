package com.example.demo;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {
    private final UUID id;

    public PostNotFoundException(UUID id) {
        super("Post: " + id + " not found");
        this.id = id;
    }

    public UUID id() {
        return id;
    }
}