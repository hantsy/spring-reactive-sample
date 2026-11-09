package com.example.demo;

import jakarta.validation.constraints.NotBlank;

public record CreatePostCommand(
        @NotBlank String title,
        @NotBlank String content) {
}
