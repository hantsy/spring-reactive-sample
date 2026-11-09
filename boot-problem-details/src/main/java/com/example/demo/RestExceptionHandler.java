package com.example.demo;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.URI;
import java.util.Map;

/**
 * Only handles the application specific exceptions. The framework exceptions are
 * converted into Problem Details by Spring Boot's built-in
 * {@code ProblemDetailsExceptionHandler}, enabled via
 * {@code spring.webflux.problemdetails.enabled=true}.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(PostNotFoundException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setType(URI.create("https://example.com/errors/post-not-found"));
        problemDetail.setProperty("id", exception.getPostId());
        problemDetail.setProperty("entity", "POST");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleWebExchangeBindException(WebExchangeBindException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request is invalid."
        );
        problemDetail.setProperty("errors",
                exception.getFieldErrors().stream()
                        .map(error -> Map.of(
                                "path", error.getField(),
                                "details", error.getDefaultMessage()))
                        .toList()
        );
        return ResponseEntity.badRequest().body(problemDetail);
    }
}
