package com.example.demo;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostCommand(
        @NotBlank String title,
        @NotBlank String content) {
}
