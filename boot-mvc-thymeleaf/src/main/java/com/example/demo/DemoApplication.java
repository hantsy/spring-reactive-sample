package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}

@Component
@Slf4j
class DataInitializer {

    private final PostRepository posts;

    public DataInitializer(PostRepository posts) {
        this.posts = posts;
    }

    @EventListener(value = ContextRefreshedEvent.class)
    public void init() {
        log.info("start data initialization  ...");
        this.posts
            .deleteAll()
            .thenMany(

                Flux
                    .range(1, 1000)
                    .flatMap(
                        num -> this.posts.save(Post.builder().title("Title" + num).content("content of " + "Title" + num).build())
                    )
            )
            .log()
            .subscribe(
                null,
                null,
                () -> log.info("done initialization...")
            );

    }

}

@Controller
class HomeController {

    private final PostRepository posts;

    HomeController(PostRepository posts) {
        this.posts = posts;
    }

    @GetMapping("/")
    public String home(final Model model) {

        Flux<Post> postList = this.posts.findAll();
        model.addAttribute("posts", new ReactiveDataDriverContextVariable(postList, 100));
        return "home";
    }

}


interface PostRepository extends ReactiveMongoRepository<Post, String>{}

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
class Post {
    
    @Id
    private String id;
    private String title;
    private String content;
    
}

