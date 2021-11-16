package com.example.demo;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Message{
  private UUID id;
  private String body;
  private LocalDateTime sentAt;
}
