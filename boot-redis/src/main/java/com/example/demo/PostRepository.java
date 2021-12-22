package com.example.demo;

import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

  Mono<Boolean> saveAll(List<Post> data) {
    Map<String , Post> dataMap = new HashMap<>();
    data.forEach(p -> dataMap.putIfAbsent(p.getId(), p));
    return template.<String, Post>opsForHash().putAll("posts", dataMap)
            .log();
  }

  Mono<Void> deleteById(String id) {
    return template.<String, Post>opsForHash().remove("posts", id)
        .flatMap(p -> Mono.<Void>empty());
  }

  Mono<Boolean> deleteAll() {
    return template.<String, Post>opsForHash().delete("posts");
  }

}
