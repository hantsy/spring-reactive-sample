package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePostNotFoundException(PostNotFoundException e) {
        var error = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        error.setType(URI.create("http://localhost:8080/errors/404"));
        error.setProperty("id", e.id());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}