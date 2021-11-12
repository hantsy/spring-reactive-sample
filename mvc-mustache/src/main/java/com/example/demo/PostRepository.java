package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PostRepository {

  private static final List<Post> DATA = new ArrayList<>();

  static {
    IntStream.of(1, 100)
        .forEachOrdered(i -> DATA.add(
            Post.builder().id(UUID.randomUUID()).title("post #" + i).content("content of post#" + i)
                .build()));

  }

  public Flux<Post> findAll() {
    return Flux.fromIterable(DATA);
  }

  Mono<Post> findById(UUID id) {
    return findAll().filter(p -> p.getId().equals(id)).last();
  }

  Mono<Post> save(Post post) {
    Post saved = Post.builder().id(UUID.randomUUID()).title(post.getTitle())
        .content(post.getContent()).build();
    log.debug("saved post: {}", saved);
    DATA.add(saved);
    return Mono.just(saved);
  }

}
