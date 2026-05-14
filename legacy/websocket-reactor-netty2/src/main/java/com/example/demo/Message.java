package com.example.demo;

import java.time.LocalDateTime;
import java.util.UUID;

record Message(
        UUID id,
        String body,
        LocalDateTime sentAt) {
}
