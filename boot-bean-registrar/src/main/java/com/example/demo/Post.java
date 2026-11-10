package com.example.demo;

import java.util.UUID;

public record Post(UUID id, String title, String content) {
}