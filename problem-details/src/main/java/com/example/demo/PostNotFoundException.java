package com.example.demo;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {

    private final UUID postId;

    public PostNotFoundException(UUID postId) {
        super("Post #" + postId + " was not found.");
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }
}
