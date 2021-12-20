package com.example.demo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

//@RedisHash("posts")
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
