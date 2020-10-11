package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.notFound;

@SpringBootApplication
@EnableNeo4jAuditing
@EnableTransactionManagement
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public ReactiveTransactionManager reactiveTransactionManager(Driver driver) {
		return  new ReactiveNeo4jTransactionManager(driver);
	}
}

@Component
@Slf4j
@RequiredArgsConstructor
class DataInitializer implements CommandLineRunner {

	private final PostRepository posts;

	@Override
	public void run(String[] args) {
		log.info("start data initialization...");
		this.posts.deleteAll()
				.thenMany(
						Flux
								.just("Post one", "Post two")
								.flatMap(
										title -> this.posts.save(Post.builder().title(title).content("The content of " + title).build())
								)
				)
				.log()
				.thenMany(
						this.posts.findAll()
				)
				.blockLast(Duration.ofSeconds(5));// to make `IntegrationTests` work.

	}

}

@RestController()
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
class PostController {

	private final PostRepository posts;

	@GetMapping("")
	public Flux<Post> all() {
		return this.posts.findAll();
	}

	@PostMapping("")
	public Mono<Post> create(@RequestBody Post post) {
		return this.posts.save(post);
	}

	@GetMapping("/{id}")
	public Mono<Post> get(@PathVariable("id") Long id) {
		return Mono.just(id)
				.flatMap(posts::findById)
				.switchIfEmpty(Mono.error(new PostNotFoundException(id)));
	}

	@PutMapping("/{id}")
	public Mono<Post> update(@PathVariable("id") Long id, @RequestBody Post post) {
		return this.posts.findById(id)
				.map(p -> {
					p.setTitle(post.getTitle());
					p.setContent(post.getContent());

					return p;
				})
				.flatMap(this.posts::save);
	}

	@DeleteMapping("/{id}")
	public Mono<Void> delete(@PathVariable("id") Long id) {
		return this.posts.deleteById(id);
	}

}

@RestControllerAdvice
@Slf4j
class RestExceptionHandler {

	@ExceptionHandler(PostNotFoundException.class)
	ResponseEntity postNotFound(PostNotFoundException ex) {
		log.debug("handling exception::" + ex);
		return notFound().build();
	}

}

class PostNotFoundException extends RuntimeException {

	PostNotFoundException(Long id) {
		super("Post #" + id + " was not found");
	}
}

interface PostRepository extends ReactiveNeo4jRepository<Post, Long> {
}

@Node
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Post {

	@Id
	@GeneratedValue
	private Long id;
	private String title;
	private String content;

	@CreatedDate
	private LocalDateTime createdDate;
}