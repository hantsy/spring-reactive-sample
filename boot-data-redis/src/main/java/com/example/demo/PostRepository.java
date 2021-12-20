package com.example.demo;

import java.util.UUID;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class PostRepository {

  ReactiveRedisOperations<String, Post> template;

  public PostRepository(ReactiveRedisOperations<String, Post> template) {
    this.template = template;
  }

  Flux<Post> findAll() {
    return template.<String, Post>opsForHash().values("posts");
  }

  Mono<Post> findById(String id) {
    return template.<String, Post>opsForHash().get("posts", id);
  }

  Mono<Post> save(Post post) {
    if (post.getId() != null) {
      String id = UUID.randomUUID().toString();
      post.setId(id);
    }
    return template.<String, Post>opsForHash().put("posts", post.getId(), post)
        .log()
        .map(p -> post);

  }

  Mono<Void> deleteById(String id) {
    return template.<String, Post>opsForHash().remove("posts", id)
        .flatMap(p -> Mono.<Void>empty());
  }

  Mono<Boolean> deleteAll() {
    return template.<String, Post>opsForHash().delete("posts");
  }

}
