package com.example.demo;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

  @Id
  private String id;
  private String title;
  private String content;

  @CreatedDate
  private LocalDateTime createdDate;
}
