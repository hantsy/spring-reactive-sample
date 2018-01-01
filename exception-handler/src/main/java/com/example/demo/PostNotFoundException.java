package com.example.demo;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long id) {
        super("Post:" + id +" is not found.");
    }
}
